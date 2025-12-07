package com.magidc.ideavim.anyobject.handlers

import com.maddyhome.idea.vim.common.TextRange
import com.magidc.ideavim.anyobject.handlers.base.TextBasedHandler

class AnyDocumentHandler() : TextBasedHandler() {
    override fun findTextSelection(text: CharSequence, textOffset: Int, caretOffset: Int, isInner: Boolean, size: Int): TextRange {
        return TextRange(0, text.length)
    }
}
