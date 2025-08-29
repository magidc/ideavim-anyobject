package com.magidc.ideavim.anyObject.handlers

import com.magidc.ideavim.anyobject.handlers.BaseHandlers
import com.magidc.ideavim.anyobject.handlers.DelimiterHandler

class AnyQuoteHandlers : BaseHandlers {
    private val delimiters = listOf("\"" to "\"", "'" to "'", "`" to "`")
    override fun getInnerHandler(): DelimiterHandler {
        return DelimiterHandler(true, true, delimiters)
    }

    override fun getOuterHandler(): DelimiterHandler {
        return DelimiterHandler(false, true, delimiters)
    }
}