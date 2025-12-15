package com.magidc.ideavim.anyobject.utils

import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.api.getLineEndForOffset
import com.maddyhome.idea.vim.common.ChangesListener
import com.maddyhome.idea.vim.common.TextRange
import com.maddyhome.idea.vim.state.mode.inBlockSelection
import com.maddyhome.idea.vim.state.mode.inSelectMode
import com.magidc.ideavim.anyobject.handlers.base.getCareOffset
import com.magidc.ideavim.anyobject.utils.TSModelExtensions.Companion.lastNamedLeafOrSelf
import com.magidc.ideavim.anyobject.utils.TSModelExtensions.Companion.nextNamedLeaf
import com.magidc.ideavim.anyobject.utils.TSModelExtensions.Companion.parentPrevNamedSibling
import com.magidc.ideavim.anyobject.utils.TSModelExtensions.Companion.prevNamedLeaf
import org.treesitter.TSInputEdit
import org.treesitter.TSNode
import org.treesitter.TSParser
import org.treesitter.TSPoint
import org.treesitter.TSTree
import java.nio.charset.StandardCharsets
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

        private fun getParser(editor: VimEditor): TSParser {
            val tsLanguageInfo = TSLanguageUtils.getLanguage(editor)
            return parserCache.getOrPut(tsLanguageInfo.name) {
                val parser = TSParser()
                tsLanguageInfo.tsLanguage?.let { parser.setLanguage(it.invoke()) }
                parser
            }
        }
    }

    private val parser: TSParser = getParser(editor)
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

    fun toCharOffset(byteIndex: Int): Int {
        if (byteIndex == 0 || byteToCharOffsetTree.isEmpty()) return byteIndex
        return byteToCharOffsetTree.headSet(OffsetDelta(byteIndex)).asSequence().map { it.delta }.sum() + byteIndex
    }

    fun toTextRange(fromNode: TSNode, toNode: TSNode = fromNode): TextRange {
        return TextRange(toCharOffset(fromNode.startByte), toCharOffset(toNode.endByte))
    }

    private fun findCurrentNode(): TSNode? {
        if (!updated) loadTSTree()
        val byteOffset = toByteOffset(editor.getCareOffset())
        var node = tsTree.rootNode
        while (node.startByte <= byteOffset) {
            val nextNode = node.getFirstChildForByte(byteOffset)
            if (nextNode.isNull || node.startByte > byteOffset) break
            node = nextNode
        }
        return node
    }

    private fun findObjectNode(currentNode: TSNode, acceptNode: (TSNode) -> Boolean): TSNode? {
        return generateSequence(currentNode) { it.parent.takeIf { n -> !n.isNull } }.firstOrNull { acceptNode(it) }
    }

    fun findSelectionNode(acceptNode: (TSNode) -> Boolean): TSNode? {
        if (disabled) return null
        val currentNode = findCurrentNode() ?: return null
        return findObjectNode(currentNode, acceptNode) ?: findNextNode(currentNode, acceptNode, loop = false)
    }

    fun findNextNode(currentNode: TSNode, acceptNode: (TSNode) -> Boolean, forward: Boolean = true, includeSelf: Boolean = true, loop: Boolean = true): TSNode? {
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
            if (forward) it.nextNamedLeaf()
            else {
                if (it.prevNamedSibling.isNull) it.parentPrevNamedSibling()?.lastNamedLeafOrSelf()
                else it.prevNamedSibling
            }
        }
        val nextNodeFunction = if (forward) { n: TSNode -> n.nextNamedLeaf() } else { n: TSNode -> n.prevNamedLeaf() }
        @Suppress("unused")
        for (i in 1..2) {
            generateSequence(node) { nextNodeFunction(it) }.filter { acceptNode(it) }.firstOrNull()?.let { return it }
            if (!loop) break
            node = tsTree.rootNode.let { if (forward) it else it.lastNamedLeafOrSelf() }
        }
        return null
    }

    fun findJumpElementOffset(acceptNode: (TSNode) -> Boolean, forward: Boolean): Int? {
        if (disabled) return null
        val currentNode = findCurrentNode() ?: return null
        val selection = editor.inSelectMode || editor.inBlockSelection
        return findNextNode(currentNode, acceptNode, forward, !selection || forward)
            ?.let { toCharOffset(if (selection) it.endByte - 1 else it.startByte) }
    }
}