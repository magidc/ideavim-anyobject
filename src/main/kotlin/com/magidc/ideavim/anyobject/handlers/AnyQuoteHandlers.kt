package com.magidc.ideavim.anyobject.handlers

import com.magidc.ideavim.anyobject.handlers.base.DelimiterHandler
import com.magidc.ideavim.anyobject.handlers.base.TextBasedHandlerFactory

class AnyQuoteHandlers : TextBasedHandlerFactory {
    companion object {
        private val delimiters = listOf("\"" to "\"", "'" to "'", "`" to "`")
    }

    override fun getInnerHandler(): DelimiterHandler {
        return DelimiterHandler(isInner = true, sameLine = true, delimiterPairs = delimiters)
    }

    override fun getOuterHandler(): DelimiterHandler {
        return DelimiterHandler(isInner = false, sameLine = true, delimiterPairs = delimiters)
    }
}