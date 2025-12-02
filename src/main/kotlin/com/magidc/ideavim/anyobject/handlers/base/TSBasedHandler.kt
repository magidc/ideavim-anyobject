package com.magidc.ideavim.anyobject.handlers.base

import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.common.TextRange
import com.magidc.ideavim.anyobject.utils.LRUCache
import com.magidc.ideavim.anyobject.utils.TSDocument
import fleet.util.letIf
import org.treesitter.TSNode


abstract class AbstractTSBasedHandler : BaseSelectionHandler, BaseJumpHandler {
    companion object {
        private val documentCache = LRUCache<String, TSDocument>(5) { _, v -> v.editor.document.removeChangeListener(v) }

        fun TSNode.toText(text: String, textLimit: Int = 20): String {
            val string = String(text.toByteArray().copyOfRange(startByte, endByte))
            return "${grammarType}: ${string.take(textLimit)}"
        }

        fun TSNode.isEqual(a: TSNode?) = a?.startByte == startByte && a.endByte == endByte

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

        fun TSNode.getFirstChildWithGrammar(grammar: String, offset: Int): TSNode? {
            var node = this
            while (node.startByte <= this.endByte) {
                if (node.endByte >= offset && node.grammarType == grammar) return node
                node = node.nextLeaf() ?: return null
            }
            return null
        }
    }

    protected fun getTSDocument(editor: VimEditor): TSDocument = documentCache.getOrPut(editor.document.toString()) { TSDocument(editor) }

    protected abstract fun acceptNode(node: TSNode): Boolean

    override fun findSelection(editor: VimEditor, inner: Boolean, size: Int): TextRange? {
        val tsDocument = getTSDocument(editor)
        return tsDocument.findSelectionNode({ acceptNode(it) })
            .letIf(inner) { findInnerBlock(it, editor.getCareOffset()) }
            ?.let { tsDocument.toTextRange(it) }
    }

    override fun allowsCountSelection(): Boolean = false

    protected open fun findInnerBlock(node: TSNode?, offset: Int): TSNode? = node?.getFirstChildWithGrammar("block", offset)

    final override fun findJumpElementStartOffset(editor: VimEditor, forward: Boolean): Int? {
        return getTSDocument(editor).findJumpElementStartOffset({ acceptNode(it) }, forward)
    }
}