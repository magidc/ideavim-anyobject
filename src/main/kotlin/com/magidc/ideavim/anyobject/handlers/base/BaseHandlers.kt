package com.magidc.ideavim.anyobject.handlers.base

import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.command.TextObjectVisualType
import com.maddyhome.idea.vim.common.TextRange

fun VimEditor.getCaretOffset(): Int = currentCaret().offset

interface BaseHandler

interface BaseSelectionHandler : BaseHandler {
    /**
     * Finds the selection to operate on.
     */
    fun findSelection(editor: VimEditor, inner: Boolean, size: Int): TextRange?
    fun allowsCountSelection(): Boolean = false
    fun getVisualType(inner: Boolean): TextObjectVisualType = TextObjectVisualType.CHARACTER_WISE
}

interface BaseJumpHandler : BaseSelectionHandler {
    /**
     * Locates the starting offset of the next or previous element to jump to.
     */
    fun findJumpElement(editor: VimEditor, forward: Boolean): TextRange?
}

