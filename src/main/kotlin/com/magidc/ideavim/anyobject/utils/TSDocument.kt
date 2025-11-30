package com.magidc.ideavim.anyobject.utils

import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiManager
import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.common.ChangesListener
import com.maddyhome.idea.vim.common.TextRange
import com.magidc.ideavim.anyobject.handlers.base.getCareOffset
import org.treesitter.TSInputEncoding
import org.treesitter.TSNode
import org.treesitter.TSParser
import org.treesitter.TSTree
import org.treesitter.TreeSitterC
import org.treesitter.TreeSitterCSharp
import org.treesitter.TreeSitterCpp
import org.treesitter.TreeSitterGo
import org.treesitter.TreeSitterJava
import org.treesitter.TreeSitterJavascript
import org.treesitter.TreeSitterJson
import org.treesitter.TreeSitterKotlin
import org.treesitter.TreeSitterObjc
import org.treesitter.TreeSitterPhp
import org.treesitter.TreeSitterPython
import org.treesitter.TreeSitterRust
import org.treesitter.TreeSitterScala
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

        private fun VimEditor.getText(): String = text().toString()

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
                    "SCALA" -> parser.setLanguage(TreeSitterScala())
                    "C#" -> parser.setLanguage(TreeSitterCSharp())
                    "RUST" -> parser.setLanguage(TreeSitterRust())
                    "GO" -> parser.setLanguage(TreeSitterGo())
                    "PYTHON" -> parser.setLanguage(TreeSitterPython())
                    "PHP" -> parser.setLanguage(TreeSitterPhp())
                    "JAVASCRIPT" -> parser.setLanguage(TreeSitterJavascript())
                    "TYPESCRIPT" -> parser.setLanguage(TreeSitterTypescript())
                    "OBJECTIVE-C" -> parser.setLanguage(TreeSitterObjc())
                    "SWIFT" -> parser.setLanguage(TreeSitterSwift())
                    "C" -> parser.setLanguage(TreeSitterC())
                    "C++" -> parser.setLanguage(TreeSitterCpp())
                    "JSON" -> parser.setLanguage(TreeSitterJson())
                    "YAML" -> parser.setLanguage(TreeSitterYaml())
                    else -> throw IllegalArgumentException("Unsupported language: $language")
                }
                parser
            }
        }
    }

    private val parser: TSParser = getParser(getLanguage(editor))
    private var tsTree: TSTree
    private var text: String = editor.getText()
    private var updated: Boolean = true

    init {
        tsTree = parser.parseStringEncoding(null, text, TSInputEncoding.TSInputEncodingUTF8)
        editor.document.addChangeListener(this)
    }

    override fun documentChanged(change: ChangesListener.Change) {
        updated = false
    }

    private fun charToByteOffset(charIndex: Int): Int {
        require(charIndex in 0..text.length)
        UTF8_ENCODER.reset()
        return UTF8_ENCODER.encode(CharBuffer.wrap(text, 0, charIndex)).limit()
    }

    private fun byteToCharOffset(byteIndex: Int): Int {
        require(byteIndex in 0..text.length)
        UTF8_DECODER.reset()
        return UTF8_DECODER.decode(ByteBuffer.wrap(text.toByteArray(), 0, byteIndex)).toString().length
    }

    private fun findCurrentNode(): TSNode? {
        if (!updated) {
            text = editor.getText()
            tsTree = parser.parseString(null, text)
            updated = true
        }
        val byteOffset = charToByteOffset(editor.getCareOffset())
        return tsTree.rootNode.getNamedDescendantForByteRange(byteOffset, byteOffset)
    }

    fun findSelection(acceptNode: (TSNode) -> Boolean): TextRange? {
        var node = findCurrentNode() ?: return null
        while (!node.isNull) {
            if (acceptNode(node))
                return TextRange(byteToCharOffset(node.startByte), byteToCharOffset(node.endByte))
            node = node.parent
        }
        return null
    }

    fun findJumpElementStartOffset(next: Boolean, acceptNode: (TSNode) -> Boolean): Int? {
        var node: TSNode? = findCurrentNode()?.let { if (next) it.nextLeaf() else it.parentPrevSibling()?.lastLeafOrSelf() } ?: return null
        val nextNodeFunction = if (next) { n: TSNode -> n.nextLeaf() } else { n: TSNode -> n.prevLeaf() }

        while (true) {
            while (null != node) {
                if (acceptNode(node)) return byteToCharOffset(node.startByte)
                node = nextNodeFunction(node)
            }
            node = tsTree.rootNode.let { if (next) it else it.lastLeafOrSelf() }
        }
    }
}