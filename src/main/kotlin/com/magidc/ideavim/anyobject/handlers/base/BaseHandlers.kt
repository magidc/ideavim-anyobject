package com.magidc.ideavim.anyobject.handlers.base

import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.common.TextRange

interface BaseHandler

interface BaseSelectionHandler : BaseHandler {
    /**
     * Finds the selection to operate on.
     */
    fun findSelection(editor: VimEditor, isInner: Boolean, size: Int): TextRange?
}

interface BaseCountSelectionHandler : BaseHandler {
    fun findSelection(editor: VimEditor, isInner: Boolean, size: Int = 1): TextRange?
}

interface BaseJumpHandler : BaseSelectionHandler {
    /**
     * Locates the starting offset of the next or previous element to jump to.
     */
    fun findJumpElementStartOffset(editor: VimEditor, next: Boolean): Int?
}

