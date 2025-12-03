package com.magidc.ideavim.anyobject.handlers.base

import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.common.TextRange
import com.magidc.ideavim.anyobject.utils.LRUCache
import com.magidc.ideavim.anyobject.utils.TSDocument
import org.treesitter.TSNode


abstract class AbstractTSBasedHandler : BaseSelectionHandler, BaseJumpHandler {

    companion object {
        private val documentCache = LRUCache<String, TSDocument>(5) { _, v -> v.editor.document.removeChangeListener(v) }
        private val innerBlockTypes = setOf("block")

        fun TSNode.toText(text: String, textLimit: Int = 20): String {
            val string = String(text.toByteArray().copyOfRange(startByte, endByte))
            return "${grammarType}: ${string.take(textLimit)}"
        }

        fun TSNode.isEqual(a: TSNode?) = (a?.isNull == false) && (a.startByte == startByte) && (a.endByte == endByte)

        fun TSNode.lastLeafOrSelf(): TSNode {
            if (namedChildCount == 0) return this
            return getNamedChild(namedChildCount - 1).lastLeafOrSelf()
        }

        fun TSNode.parentPrevSibling(): TSNode? {
            if (parent.isNull) return null
            return if (parent.prevNamedSibling.isNull) parent.parentPrevSibling() else parent.prevNamedSibling
        }

        fun TSNode.prevLeaf(): TSNode? {
            return if (prevNamedSibling.isNull) parent.takeIf { !it.isNull } else prevNamedSibling?.lastLeafOrSelf()
        }

        fun TSNode.next(): TSNode? {
            if (nextNamedSibling.isNull)
                return if (parent.isNull) null else parent.next()
            return nextNamedSibling
        }

        fun TSNode.nextLeaf(): TSNode? {
            if (namedChildCount > 0) return getNamedChild(0)
            return if (nextNamedSibling.isNull) parent.next() else nextNamedSibling
        }

        fun TSNode.getFirstChildWithGrammar(grammars: Set<String>, offset: Int): TSNode? {
            var node = this
            while (node.startByte <= this.endByte) {
                if (node.endByte >= offset && grammars.contains(node.grammarType)) return node
                node = node.nextLeaf() ?: return null
            }
            return null
        }
    }

    protected abstract val targetTypes: Set<String>

    protected fun getTSDocument(editor: VimEditor): TSDocument = documentCache.getOrPut(editor.document.toString()) { TSDocument(editor) }

    protected open fun acceptNode(node: TSNode): Boolean = !node.isNull && node.isNamed && targetTypes.contains(node.grammarType)

    override fun findSelection(editor: VimEditor, inner: Boolean, size: Int): TextRange? {
        val tsDocument = getTSDocument(editor)
        val objectNode = tsDocument.findSelectionNode { acceptNode(it) } ?: return null
        if (!inner) return tsDocument.toTextRange(objectNode)
        return findInnerBlock(objectNode, editor.getCareOffset())
            ?.takeIf { it.namedChildCount > 0 }
            ?.let { tsDocument.toTextRange(it.getNamedChild(0), it.getNamedChild(it.namedChildCount - 1)) }
            ?: tsDocument.toTextRange(objectNode)
    }

    override fun allowsCountSelection(): Boolean = false

    protected open fun findInnerBlock(node: TSNode?, offset: Int): TSNode? = node?.getFirstChildWithGrammar(innerBlockTypes, offset)

    final override fun findJumpElementStartOffset(editor: VimEditor, forward: Boolean): Int? {
        return getTSDocument(editor).findJumpElementStartOffset({ acceptNode(it) }, forward)
    }
}