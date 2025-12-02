package com.magidc.ideavim.anyobject.handlers.base

import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.common.TextRange
import com.magidc.ideavim.anyobject.utils.LRUCache
import com.magidc.ideavim.anyobject.utils.TSDocument
import org.treesitter.TSNode


abstract class AbstractTSBasedHandler : BaseSelectionHandler, BaseJumpHandler {
    companion object {
        private val documentCache = LRUCache<String, TSDocument>(5) { _, v -> v.editor.document.removeChangeListener(v) }
        private fun getTSDocument(editor: VimEditor): TSDocument = documentCache.getOrPut(editor.document.toString()) { TSDocument(editor) }
    }

    protected abstract fun acceptNode(node: TSNode): Boolean

    final override fun findSelection(editor: VimEditor, isInner: Boolean, size: Int): TextRange? {
        return getTSDocument(editor).findSelection({ acceptNode(it) }, if (allowsCountSelection()) size else 1)
    }

    override fun allowsCountSelection(): Boolean = false

    final override fun findJumpElementStartOffset(editor: VimEditor, forward: Boolean): Int? {
        return getTSDocument(editor).findJumpElementStartOffset({ acceptNode(it) }, forward)
    }
}