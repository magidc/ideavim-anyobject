package com.magidc.ideavim.anyobject.handlers.base

import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.state.mode.Mode
import com.maddyhome.idea.vim.state.mode.SelectionType
import com.magidc.ideavim.anyobject.model.Selection

interface BaseHandler

interface BaseSelectionHandler : BaseHandler {
    /**
     * Finds the selection to operate on.
     */
    fun findSelection(editor: VimEditor, isInner: Boolean, size: Int): Selection?

    /**
     * Applies the action (pending operation or visual selection) to the given selection.
     */
    fun executeSelection(editor: VimEditor, isInner: Boolean, selectionSize: Int = 1) {
        val selection = findSelection(editor, isInner, selectionSize) ?: return
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

interface BaseJumpHandler : BaseSelectionHandler {
    /**
     * Locates the starting offset of the next or previous element to jump to.
     */
    fun findJumpElementStartOffset(editor: VimEditor, next: Boolean): Int?

    /**
     * Moves the caret to the next or previous element to jump to.
     */
    fun executeJump(editor: VimEditor, next: Boolean) {
        val jumpElementStartOffset = findJumpElementStartOffset(editor, next) ?: return
        editor.currentCaret().moveToOffset(jumpElementStartOffset)
    }
}

