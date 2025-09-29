package com.magidc.ideavim.anyobject.handlers

import com.magidc.ideavim.anyobject.handlers.base.TextBasedHandler
import com.magidc.ideavim.anyobject.model.Selection


class AnyDocumentHandler() : TextBasedHandler() {
    override fun findTextSelection(text: CharSequence, textOffset: Int, caretOffset: Int, isInner: Boolean): Selection {
        return Selection(0, text.length)
    }
}
