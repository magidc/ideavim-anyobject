package com.magidc.ideavim.anyobject.ts.base

import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.common.TextRange
import com.magidc.ideavim.anyobject.handlers.base.TSBasedHandler
import com.magidc.ideavim.anyobject.utils.TSDocument
import org.treesitter.TSNode

class TestTSHandlerWrapper(val handler: TSBasedHandler) : TSBasedHandler() {
    override val targetTypes: Set<String> get() = TODO("Unused")

    override fun findSelection(editor: VimEditor, inner: Boolean, size: Int): TextRange? = handler.findSelection(editor, inner, size)

    override fun allowsCountSelection(): Boolean = handler.allowsCountSelection()

    override fun findInnerBlockRange(currentNode: TSNode, objectNode: TSNode, byteOffset: Int, tsDocument: TSDocument): TextRange? =
        handler.findInnerBlockRange(currentNode, objectNode, byteOffset, tsDocument)
}