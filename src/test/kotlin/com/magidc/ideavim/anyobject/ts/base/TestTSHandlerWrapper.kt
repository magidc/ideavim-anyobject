package com.magidc.ideavim.anyobject.ts.base

import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.common.TextRange
import com.magidc.ideavim.anyobject.handlers.base.TSBasedHandler
import com.magidc.ideavim.anyobject.utils.TSDocument
import org.treesitter.TSNode

class TestTSHandlerWrapper(val handler: TSBasedHandler) : TSBasedHandler() {
    override val targetTypes: Set<String> get() = TODO("Unused")

    val unusedTargetTypes = handler.targetTypes.toMutableSet()
    val sortedTargetTypes = handler.targetTypes.toMutableSet().toList().sortedBy { it }

    init {
        val handlerAcceptNode = handler.acceptNode
        handler.acceptNode = { n ->
            if (handlerAcceptNode(n)) {
                unusedTargetTypes.remove(n.grammarType)
                true
            } else false
        }
    }

    override fun findSelection(editor: VimEditor, inner: Boolean, size: Int): TextRange? = handler.findSelection(editor, inner, size)

    override fun allowsCountSelection(): Boolean = handler.allowsCountSelection()

    override fun findInnerBlockRange(node: TSNode, offset: Int, tsDocument: TSDocument): TextRange? = handler.findInnerBlockRange(node, offset, tsDocument)
}