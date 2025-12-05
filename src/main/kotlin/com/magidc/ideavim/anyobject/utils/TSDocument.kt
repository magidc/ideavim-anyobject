package com.magidc.ideavim.anyobject.utils

import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiManager
import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.common.ChangesListener
import com.maddyhome.idea.vim.common.TextRange
import com.magidc.ideavim.anyobject.handlers.base.AbstractTSBasedHandler.Companion.lastLeafOrSelf
import com.magidc.ideavim.anyobject.handlers.base.AbstractTSBasedHandler.Companion.nextLeaf
import com.magidc.ideavim.anyobject.handlers.base.AbstractTSBasedHandler.Companion.parentPrevSibling
import com.magidc.ideavim.anyobject.handlers.base.AbstractTSBasedHandler.Companion.prevLeaf
import com.magidc.ideavim.anyobject.handlers.base.getCareOffset
import org.treesitter.TSInputEdit
import org.treesitter.TSLanguage
import org.treesitter.TSNode
import org.treesitter.TSParser
import org.treesitter.TSPoint
import org.treesitter.TSTree
import org.treesitter.TreeSitterCSharp
import org.treesitter.TreeSitterClojure
import org.treesitter.TreeSitterCpp
import org.treesitter.TreeSitterCss
import org.treesitter.TreeSitterGo
import org.treesitter.TreeSitterHtml
import org.treesitter.TreeSitterJava
import org.treesitter.TreeSitterJavascript
import org.treesitter.TreeSitterJson
import org.treesitter.TreeSitterKotlin
import org.treesitter.TreeSitterObjc
import org.treesitter.TreeSitterPhp
import org.treesitter.TreeSitterPython
import org.treesitter.TreeSitterR
import org.treesitter.TreeSitterRuby
import org.treesitter.TreeSitterRust
import org.treesitter.TreeSitterScala
import org.treesitter.TreeSitterSql
import org.treesitter.TreeSitterSwift
import org.treesitter.TreeSitterTypescript
import org.treesitter.TreeSitterYaml
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.TreeSet
import java.util.concurrent.atomic.AtomicInteger

class TSDocument( val editor: VimEditor) : ChangesListener {
    companion object {
        private class OffsetDelta(val sourceOffset: Int, val delta: Int = 0) : Comparable<OffsetDelta> {
            override fun compareTo(other: OffsetDelta): Int = sourceOffset.compareTo(other.sourceOffset)
        }

        private class LineOffset(val startOffset: Int, val line: Int = -1) : Comparable<LineOffset> {
            override fun compareTo(other: LineOffset): Int = startOffset.compareTo(other.startOffset)
        }

        private val FIRST_OFFSET_DELTA = OffsetDelta(0)
        private val parserCache = LRUCache<String, TSParser>(3)

        private fun getLanguage(editor: VimEditor): String? {
            val vimVirtualFile = editor.getVirtualFile() ?: return null
            val projectManager = ProjectManager.getInstance()
            if (null == projectManager || projectManager.openProjects.isEmpty()) return null
            val project = projectManager.openProjects[0]
            val virtualFile = VirtualFileManager.getInstance().findFileByNioPath(Path.of(vimVirtualFile.path)) ?: return null
            return PsiManager.getInstance(project).findFile(virtualFile)?.language?.displayName
        }

        private fun getParserFromApp(): TSLanguage? {
            val ideName = ApplicationInfo.getInstance().fullApplicationName.lowercase()
            if (ideName.contains("intellij")) return TreeSitterJava()
            if (ideName.contains("pycharm")) return TreeSitterPython()
            if (ideName.contains("rustrover")) return TreeSitterRust()
            if (ideName.contains("rider")) return TreeSitterCSharp()
            if (ideName.contains("webstorm")) return TreeSitterJavascript()
            if (ideName.contains("phpstorm")) return TreeSitterPhp()
            if (ideName.contains("rubymine")) return TreeSitterRuby()
            if (ideName.contains("goland")) return TreeSitterGo()
            if (ideName.contains("clion")) return TreeSitterCpp()
            if (ideName.contains("datagrip")) return TreeSitterSql()
            if (ideName.contains("android")) return TreeSitterKotlin()
            if (ideName.contains("appcode")) return TreeSitterSwift()

            return null
        }

        private fun getParser(language: String?): TSParser {
            if (null == language || language.isBlank()) throw IllegalArgumentException()
            return parserCache.getOrPut(language) {
                val parser = TSParser()
                when (language.uppercase()) {
                    "JAVA" -> parser.setLanguage(TreeSitterJava())
                    "KOTLIN" -> parser.setLanguage(TreeSitterKotlin())
                    "CLOJURE" -> parser.setLanguage(TreeSitterClojure())
                    "SCALA" -> parser.setLanguage(TreeSitterScala())
                    "C#" -> parser.setLanguage(TreeSitterCSharp())
                    "RUST" -> parser.setLanguage(TreeSitterRust())
                    "GO" -> parser.setLanguage(TreeSitterGo())
                    "PYTHON" -> parser.setLanguage(TreeSitterPython())
                    "PHP" -> parser.setLanguage(TreeSitterPhp())
                    "HTML" -> parser.setLanguage(TreeSitterHtml())
                    "CSS" -> parser.setLanguage(TreeSitterCss())
                    "ECMAScript 6" -> parser.setLanguage(TreeSitterJavascript())
                    "TYPESCRIPT" -> parser.setLanguage(TreeSitterTypescript())
                    "OBJECTIVE-C" -> parser.setLanguage(TreeSitterObjc())
                    "SWIFT" -> parser.setLanguage(TreeSitterSwift())
                    "C/C++" -> parser.setLanguage(TreeSitterCpp())
                    "R" -> parser.setLanguage(TreeSitterR())
                    "SQL" -> parser.setLanguage(TreeSitterSql())
                    "JSON" -> parser.setLanguage(TreeSitterJson())
                    "YAML" -> parser.setLanguage(TreeSitterYaml())
                    else -> getParserFromApp()?.let { parser.setLanguage(it) }
                }
                parser
            }
        }
    }

    private val parser: TSParser = getParser(getLanguage(editor))
    private lateinit var tsTree: TSTree
    private val disabled: Boolean
    private var updated: Boolean = false
    private val charToByteOffsetTree = TreeSet<OffsetDelta>()
    private val byteToCharOffsetTree = TreeSet<OffsetDelta>()
    private val lineStartOffsetTree = TreeSet<LineOffset>()

    init {
        if (parser.language == null)
            disabled = true
        else {
            disabled = false
            loadTSTree()
            editor.document.addChangeListener(this)
        }
    }

    private fun editDocument(change: ChangesListener.Change, text: String): TSTree? {
        val startByte = toByteOffset(change.offset)
        val startPoint = findTSPoint(startByte) ?: return null
        val oldEndByte = toByteOffset(change.offset + change.oldFragment.length)
        val oldEndPoint = findTSPoint(oldEndByte) ?: return null
        val newEndByte = toByteOffset(change.offset + change.newFragment.length)
        val newEndPoint = findTSPoint(newEndByte) ?: return null

        tsTree.edit(TSInputEdit(startByte, oldEndByte, newEndByte, startPoint, oldEndPoint, newEndPoint))
        return parser.parseString(tsTree, text)
    }

    override fun documentChanged(change: ChangesListener.Change) {
        val text = editor.text().toString()
        val updatedTSTree = editDocument(change, text)
        if (null != updatedTSTree) {
            tsTree = updatedTSTree
            reloadCacheTrees(text)
            updated = true
        } else updated = false
    }

    private fun findTSPoint(byteOffSet: Int): TSPoint? {
        return lineStartOffsetTree.floor(LineOffset(byteOffSet))?.let { TSPoint(it.line, byteOffSet - it.startOffset) }
    }

    private fun loadTSTree() {
        val text = editor.text().toString()
        tsTree = parser.parseString(null, text)
        reloadCacheTrees(text)
        updated = true
    }

    private fun reloadCacheTrees(text: String) {
        // As byte offsets do not always match char offsets (i.e., emojis), we need to calculate the difference between them
        // Trees are used to track those offsets where there are differences so we can efficiently convert between byte and char offsets
        charToByteOffsetTree.clear()
        byteToCharOffsetTree.clear()
        val charArray = text.toCharArray()
        val byteCount = AtomicInteger()
        for (i in 0 until charArray.size) {
            val byteDelta = charArray[i].toString().toByteArray(StandardCharsets.UTF_8).size - 1
            if (byteDelta > 0) {
                charToByteOffsetTree.add(OffsetDelta(i, byteDelta))
                byteToCharOffsetTree.add(OffsetDelta(byteCount.get() + 1, -byteDelta))
            }
            byteCount.addAndGet(byteDelta + 1)
        }
        lineStartOffsetTree.clear()
        for (lineIdx in 0 until editor.lineCount())
            lineStartOffsetTree.add(LineOffset(toByteOffset(editor.getLineStartOffset(lineIdx)), lineIdx))
    }

    private fun toByteOffset(charIndex: Int): Int {
        if (charIndex == 0 || charToByteOffsetTree.isEmpty()) return charIndex
        return charToByteOffsetTree.subSet(FIRST_OFFSET_DELTA, OffsetDelta(charIndex)).asSequence().map { it.delta }.sum() + charIndex
    }

    private fun toCharOffset(byteIndex: Int): Int {
        if (byteIndex == 0 || byteToCharOffsetTree.isEmpty()) return byteIndex
        return byteToCharOffsetTree.subSet(FIRST_OFFSET_DELTA, OffsetDelta(byteIndex)).asSequence().map { it.delta }.sum() + byteIndex
    }

    fun toTextRange(fromNode: TSNode, toNode: TSNode = fromNode): TextRange {
        return TextRange(toCharOffset(fromNode.startByte), toCharOffset(toNode.endByte))
    }

    private fun findCurrentNode(offset: Int): TSNode? {
        if (!updated) loadTSTree()
        val byteOffset = toByteOffset(offset)
        val containerNode = tsTree.rootNode.getFirstChildForByte(byteOffset)
        if (containerNode.startByte > byteOffset) return containerNode
        return containerNode.getDescendantForByteRange(byteOffset, byteOffset)
    }

    private fun findObjectNode(currentNode: TSNode, acceptNode: (TSNode) -> Boolean): TSNode? {
        var node = currentNode
        while (!node.isNull) {
            if (acceptNode(node)) return node
            node = node.parent
        }
        return null
    }

    fun findSelectionNode(acceptNode: (TSNode) -> Boolean): TSNode? {
        if (disabled) return null
        val currentNode = findCurrentNode(editor.getCareOffset()) ?: return null
        return findObjectNode(currentNode, acceptNode) ?: findNextNode(currentNode, acceptNode)
    }

    fun findNextNode(currentNode: TSNode, acceptNode: (TSNode) -> Boolean, forward: Boolean = true): TSNode? {
        var node: TSNode? = currentNode.let {
            if (forward)
                it.nextLeaf()
            else {
                if (it.prevNamedSibling.isNull)
                    it.parentPrevSibling()?.lastLeafOrSelf()
                else it.prevNamedSibling
            }
        }
        val nextNodeFunction = if (forward) { n: TSNode -> n.nextLeaf() } else { n: TSNode -> n.prevLeaf() }
        @Suppress("unused")
        for (i in 1..2) {
            while (null != node) {
                if (acceptNode(node)) return node
                node = nextNodeFunction(node)
            }
            node = tsTree.rootNode.let { if (forward) it else it.lastLeafOrSelf() }
        }
        return null
    }

    fun findJumpElementStartOffset(acceptNode: (TSNode) -> Boolean, forward: Boolean): Int? {
        if (disabled) return null
        val caretOffset = editor.getCareOffset()
        val currentNode = findCurrentNode(caretOffset) ?: return null
        if (acceptNode(currentNode)) {
            val currentNodeStartOffset = toCharOffset(currentNode.startByte)
            if (forward) {
                if (currentNodeStartOffset > caretOffset) return currentNodeStartOffset
            } else if (currentNodeStartOffset < caretOffset) return currentNodeStartOffset
        }
        return findNextNode(currentNode, acceptNode, forward)?.let { toCharOffset(it.startByte) }
    }
}