package com.magidc.ideavim.anyobject.utils

import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiManager
import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.common.ChangesListener
import com.maddyhome.idea.vim.common.TextRange
import com.magidc.ideavim.anyobject.handlers.base.getCareOffset
import org.treesitter.TSNode
import org.treesitter.TSParser
import org.treesitter.TSTree
import org.treesitter.TreeSitterC
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
import org.treesitter.TreeSitterRust
import org.treesitter.TreeSitterScala
import org.treesitter.TreeSitterSql
import org.treesitter.TreeSitterSwift
import org.treesitter.TreeSitterTypescript
import org.treesitter.TreeSitterYaml
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.StandardCharsets
import java.nio.file.Path

class TSDocument(val editor: VimEditor) : ChangesListener {
    companion object {
        private val UTF8_ENCODER = StandardCharsets.UTF_8.newEncoder()
        private val UTF8_DECODER = StandardCharsets.UTF_8.newDecoder()

        private fun TSNode.toText(text: String, textLimit: Int = 20): String {
            val string = String(text.toByteArray().copyOfRange(startByte, endByte))
            return "${grammarType}: ${string.take(textLimit)}"
        }

        private fun TSNode.lastLeafOrSelf(): TSNode {
            if (namedChildCount == 0) return this
            return getNamedChild(namedChildCount - 1).lastLeafOrSelf()
        }

        private fun TSNode.parentPrevSibling(): TSNode? {
            if (parent.isNull) return null
            return if (parent.prevNamedSibling.isNull) parent.parentPrevSibling() else parent.prevNamedSibling
        }

        private fun TSNode.prevLeaf(): TSNode? {
            return if (prevNamedSibling.isNull) parent.takeIf { !it.isNull } else prevNamedSibling?.lastLeafOrSelf()
        }

        private fun TSNode.next(): TSNode? {
            if (nextNamedSibling.isNull)
                return if (parent.isNull) null else parent.next()
            return nextNamedSibling
        }

        private fun TSNode.nextLeaf(): TSNode? {
            if (namedChildCount > 0) return getNamedChild(0)
            return if (nextNamedSibling.isNull) parent.next() else nextNamedSibling
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
                    "JAVASCRIPT" -> parser.setLanguage(TreeSitterJavascript())
                    "TYPESCRIPT" -> parser.setLanguage(TreeSitterTypescript())
                    "OBJECTIVE-C" -> parser.setLanguage(TreeSitterObjc())
                    "SWIFT" -> parser.setLanguage(TreeSitterSwift())
                    "C" -> parser.setLanguage(TreeSitterC())
                    "C++" -> parser.setLanguage(TreeSitterCpp())
                    "R" -> parser.setLanguage(TreeSitterR())
                    "SQL" -> parser.setLanguage(TreeSitterSql())
                    "JSON" -> parser.setLanguage(TreeSitterJson())
                    "YAML" -> parser.setLanguage(TreeSitterYaml())
                }
                parser
            }
        }
    }

    private val parser: TSParser = getParser(getLanguage(editor))
    private lateinit var tsTree: TSTree
    private lateinit var text: String
    private var updated: Boolean = false

    init {
        loadTSTree()
        editor.document.addChangeListener(this)
    }

    override fun documentChanged(change: ChangesListener.Change) {
        updated = false
    }

    private fun loadTSTree() {
        text = editor.text().toString()
        tsTree = parser.parseString(null, text)
        updated = true
    }

    private fun charToByteOffset(charIndex: Int): Int {
        require(charIndex in 0..text.length)
        UTF8_ENCODER.reset()
        return UTF8_ENCODER.encode(CharBuffer.wrap(text, 0, charIndex)).limit()
    }

    private fun byteToCharOffset(byteIndex: Int): Int {
        require(byteIndex in 0..tsTree.rootNode.endByte)
        UTF8_DECODER.reset()
        return UTF8_DECODER.decode(ByteBuffer.wrap(text.toByteArray(), 0, byteIndex)).toString().length
    }

    private fun findCurrentNode(offset: Int): TSNode? {
        if (!updated) loadTSTree()
        val byteOffset = charToByteOffset(offset)
        val containerNode = tsTree.rootNode.getFirstChildForByte(byteOffset)
        if(containerNode.startByte > byteOffset) return containerNode
        return containerNode.getDescendantForByteRange(byteOffset, byteOffset)
    }

    private fun findObject(currentNode: TSNode, acceptNode: (TSNode) -> Boolean): TSNode? {
        var node = currentNode
        while (!node.isNull) {
            if (acceptNode(node)) return node
            node = node.parent
        }
        return null
    }

    fun findSelection(acceptNode: (TSNode) -> Boolean, size: Int): TextRange? {
        val currentNode = findCurrentNode(editor.getCareOffset()) ?: return null
        return (findObject(currentNode, acceptNode) ?: findNext(currentNode, acceptNode))?.let { TextRange(byteToCharOffset(it.startByte), byteToCharOffset(it.endByte)) }
    }

    private fun findNext(currentNode: TSNode, acceptNode: (TSNode) -> Boolean, forward: Boolean = true): TSNode? {
        var node: TSNode? = currentNode.let {
            if (forward)
                it.nextLeaf()
            else {
                if (it.prevNamedSibling.isNull)
                    it.parentPrevSibling()?.lastLeafOrSelf()
                else it.prevNamedSibling
            }
        } ?: return null
        val nextNodeFunction = if (forward) { n: TSNode -> n.nextLeaf() } else { n: TSNode -> n.prevLeaf() }

        while (true) {
            while (null != node) {
                if (acceptNode(node)) return node
                node = nextNodeFunction(node)
            }
            node = tsTree.rootNode.let { if (forward) it else it.lastLeafOrSelf() }
        }
    }

    fun findJumpElementStartOffset(acceptNode: (TSNode) -> Boolean, forward: Boolean): Int? {
        val caretOffset = editor.getCareOffset()
        val currentNode = findCurrentNode(caretOffset) ?: return null
        if (acceptNode(currentNode)) {
            val currentNodeStartOffset = byteToCharOffset(currentNode.startByte)
            if (forward) {
                if (currentNodeStartOffset > caretOffset) return currentNodeStartOffset
            } else if (currentNodeStartOffset < caretOffset) return currentNodeStartOffset
        }
        return findNext(currentNode, acceptNode, forward)?.let { byteToCharOffset(it.startByte) }
    }
}