package com.magidc.ideavim.anyobject.utils

import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiManager
import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.api.getLineEndForOffset
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

class TSDocument(val editor: VimEditor) : ChangesListener {
    companion object {
        private fun String.byteLength(): Int = toByteArray(StandardCharsets.UTF_8).size

        private fun getUTF8ByteLength(codePoint: Int): Int =
            when {
                codePoint <= 0x7F -> 1
                codePoint <= 0x7FF -> 2
                codePoint <= 0xFFFF -> 3
                else -> 4
            }

        private class OffsetDelta(val sourceOffset: Int, val delta: Int = 0) : Comparable<OffsetDelta> {
            override fun compareTo(other: OffsetDelta): Int = sourceOffset.compareTo(other.sourceOffset)
        }

        private class LineOffset(val startOffset: Int, val line: Int = -1) : Comparable<LineOffset> {
            override fun compareTo(other: LineOffset): Int = startOffset.compareTo(other.startOffset)
        }

        private val parserCache = LRUCache<String, TSParser>(3)

        private fun getLanguage(editor: VimEditor): String? {
            val vimVirtualFile = editor.getVirtualFile() ?: return null
            val projectManager = ProjectManager.getInstance()
            if (null == projectManager || projectManager.openProjects.isEmpty()) return null
            val project = projectManager.openProjects[0]
            val virtualFile = VirtualFileManager.getInstance().findFileByNioPath(Path.of(vimVirtualFile.path)) ?: return null
            return PsiManager.getInstance(project).findFile(virtualFile)?.language?.displayName
        }

        private fun getDefaultAppLanguage(): TSLanguage? {
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
                    else -> getDefaultAppLanguage()?.let { parser.setLanguage(it) }
                }
                parser
            }
        }
    }

    private val parser: TSParser = getParser(getLanguage(editor))
    private lateinit var tsTree: TSTree
    private val disabled: Boolean = parser.language == null
    private var updated: Boolean = false
    private val charToByteOffsetTree = TreeSet<OffsetDelta>()
    private val byteToCharOffsetTree = TreeSet<OffsetDelta>()
    private val lineStartOffsetTree = TreeSet<LineOffset>()

    init {
        if (!disabled) {
            loadTSTree()
            editor.document.addChangeListener(this)
        }
    }

    @Suppress("unused")
    private fun editDocument(change: ChangesListener.Change, text: String): Boolean {
        val startByte = toByteOffset(change.offset)
        val startPoint = findTSPoint(startByte) ?: return false
        val oldEndByte = toByteOffset(startByte + change.oldFragment.byteLength())
        val oldEndPoint = findTSPoint(oldEndByte) ?: return false
        reloadCacheTrees(text, change.offset, change.offset + change.oldFragment.length)
        val newEndByte = toByteOffset(startByte + change.newFragment.byteLength())
        val newEndPoint = findTSPoint(newEndByte) ?: return false
        tsTree.edit(TSInputEdit(startByte, oldEndByte, newEndByte, startPoint, oldEndPoint, newEndPoint))
        return parser.parseString(tsTree, text)?.let { tsTree = it }?.let { true } ?: false
    }

    override fun documentChanged(change: ChangesListener.Change) {
        if (!updated) return
//        updated = editDocument(change, editor.text().toString())
        updated = false
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


    private fun reloadCacheTrees(text: String, fromOffSet: Int = 0, toOffset: Int = text.length) {
        // As byte offsets do not always match char offsets (i.e., emojis), we need to calculate the difference between them
        // Trees are used to track those offsets where there are differences so we can efficiently convert between byte and char offsets
        var byteIndex = toByteOffset(fromOffSet)
        var charIndex = fromOffSet

        if (fromOffSet == 0 && toOffset == text.length) {
            charToByteOffsetTree.clear()
            byteToCharOffsetTree.clear()
        } else {
            charToByteOffsetTree.removeIf { it.sourceOffset !in toOffset..<fromOffSet }
            byteToCharOffsetTree.removeIf { it.sourceOffset !in toByteOffset(toOffset)..<byteIndex }
        }

        while (charIndex < toOffset) {
            val codePoint = text.codePointAt(charIndex)
            // Total bytes in this character (code point)
            val byteLength = getUTF8ByteLength(codePoint)
            // Total UTF8 characters in this character (code point)
            val charCount = Character.charCount(codePoint)
            if (byteLength > 1) {
                // Extra bytes of to represent this character
                val deltaBytes = byteLength - 1
                charToByteOffsetTree.add(OffsetDelta(charIndex, deltaBytes - charCount + 1))
                // Char Idx = Byte Idx - (Extra bytes of this character) + (Extra UTF8 characters in this character)
                // For example: 😄 = 2 utf chars, 4 bytes
                byteToCharOffsetTree.add(OffsetDelta(byteIndex, (charCount - 1 - deltaBytes)))
            }
            charIndex += charCount
            byteIndex += byteLength
        }
        val fromLineIdx = editor.getLineEndForOffset(fromOffSet)
        lineStartOffsetTree.removeIf { it.line >= fromLineIdx }
        for (lineIdx in fromLineIdx until editor.lineCount())
            lineStartOffsetTree.add(LineOffset(toByteOffset(editor.getLineStartOffset(lineIdx)), lineIdx))
    }

    private fun toByteOffset(charIndex: Int): Int {
        if (charIndex == 0 || charToByteOffsetTree.isEmpty()) return charIndex
        return charToByteOffsetTree.headSet(OffsetDelta(charIndex)).asSequence().map { it.delta }.sum() + charIndex
    }

    private fun toCharOffset(byteIndex: Int): Int {
        if (byteIndex == 0 || byteToCharOffsetTree.isEmpty()) return byteIndex
        return byteToCharOffsetTree.headSet(OffsetDelta(byteIndex)).asSequence().map { it.delta }.sum() + byteIndex
    }

    fun toTextRange(fromNode: TSNode, toNode: TSNode = fromNode): TextRange {
        return TextRange(toCharOffset(fromNode.startByte), toCharOffset(toNode.endByte))
    }

    private fun findCurrentNode(offset: Int): TSNode? {
        if (!updated) loadTSTree()
        val byteOffset = toByteOffset(offset)
        var node = tsTree.rootNode
        while (node.startByte < byteOffset) {
            val nextNode = node.getFirstChildForByte(byteOffset)
            if (nextNode.isNull) break
            node = nextNode
        }
        return node
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

    fun findNextNode(currentNode: TSNode, acceptNode: (TSNode) -> Boolean, forward: Boolean = true, includeSelf: Boolean = true): TSNode? {
        val objectNode = findObjectNode(currentNode, acceptNode)
        val startNode =
            if (objectNode != null) {
                if (includeSelf) {
                    if (forward && objectNode.startByte > currentNode.startByte) return objectNode
                    if (!forward && objectNode.startByte < currentNode.startByte) return objectNode
                }
                objectNode
            } else currentNode

        var node: TSNode? = startNode.let {
            if (forward) it.nextLeaf()
            else {
                if (it.prevNamedSibling.isNull) it.parentPrevSibling()?.lastLeafOrSelf()
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

    fun findJumpElementOffset(acceptNode: (TSNode) -> Boolean, forward: Boolean): Int? {
        if (disabled) return null
        val caretOffset = editor.getCareOffset()
        val currentNode = findCurrentNode(caretOffset) ?: return null
        val selectionModel = editor.getSelectionModel()
        val selection = selectionModel.hasSelection() && (selectionModel.selectionEnd - selectionModel.selectionStart) > 1
        return findNextNode(currentNode, acceptNode, forward, !selection || forward)
            ?.let { toCharOffset(if (selection) it.endByte - 1 else it.startByte) }
    }
}