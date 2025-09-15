package com.magidc.ideavim.anyobject.handlers.base

import com.maddyhome.idea.vim.api.ExecutionContext
import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.command.OperatorArguments
import com.maddyhome.idea.vim.extension.ExtensionHandler
import com.maddyhome.idea.vim.state.mode.Mode
import com.maddyhome.idea.vim.state.mode.SelectionType
import com.magidc.ideavim.anyobject.model.Selection

interface HandlerFactory {
    fun getInnerHandler(): BaseHandler
    fun getOuterHandler(): BaseHandler
}

abstract class BaseHandler(val isInner: Boolean) : ExtensionHandler {
    abstract fun findSelection(editor: VimEditor): Selection?

    override fun execute(editor: VimEditor, context: ExecutionContext, operatorArguments: OperatorArguments) {
        val selection = findSelection(editor) ?: return
        val caret = editor.currentCaret()

        if (editor.mode is Mode.OP_PENDING) {
            caret.vimSelectionStartClear()
            caret.moveToOffset(selection.to - 1)
            editor.mode = Mode.VISUAL(SelectionType.CHARACTER_WISE)
            caret.setSelection(selection.from, selection.to)
        } else {
            caret.moveToOffset(selection.to - 1)
            caret.setSelection(selection.from, selection.to)
        }
    }
}