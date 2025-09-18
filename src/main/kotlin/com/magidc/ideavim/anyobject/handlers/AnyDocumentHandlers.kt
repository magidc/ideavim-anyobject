package com.magidc.ideavim.anyobject.handlers

import com.magidc.ideavim.anyobject.handlers.base.TextBasedHandler
import com.magidc.ideavim.anyobject.handlers.base.TextBasedHandlerFactory
import com.magidc.ideavim.anyobject.model.Selection

class AnyDocumentHandlers : TextBasedHandlerFactory {
    override fun getInnerHandler(): TextBasedHandler {
        return AnyDocumentHandler()
    }

    override fun getOuterHandler(): TextBasedHandler {
        return AnyDocumentHandler()
    }
}

class AnyDocumentHandler() : TextBasedHandler(false) {
    override fun findTextSelection(text: CharSequence, textOffset: Int, caretOffset: Int): Selection {
        return Selection(0, text.length)

    }
}
