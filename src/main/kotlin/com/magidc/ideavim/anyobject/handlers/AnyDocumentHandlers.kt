package com.magidc.ideavim.anyobject.handlers

import com.maddyhome.idea.vim.api.VimEditor
import com.magidc.ideavim.anyobject.handlers.base.BaseHandler
import com.magidc.ideavim.anyobject.handlers.base.HandlerFactory
import com.magidc.ideavim.anyobject.model.Selection

class AnyDocumentHandlers : HandlerFactory {
    override fun getInnerHandler(): BaseHandler {
        return AnyDocumentHandler()
    }

    override fun getOuterHandler(): BaseHandler {
        return AnyDocumentHandler()
    }
}

class AnyDocumentHandler() : BaseHandler(false) {

    override fun findSelection(editor: VimEditor): Selection {
        return Selection(0, editor.text().length)
    }
}
