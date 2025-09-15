package com.magidc.ideavim.anyobject.handlers

import com.magidc.ideavim.anyobject.handlers.base.DelimiterHandler
import com.magidc.ideavim.anyobject.handlers.base.DelimiterHandlerFactory

class AnyBracketHandlers : DelimiterHandlerFactory {
    companion object {
        private val delimiters = listOf("[" to "]", "{" to "}", "(" to ")", "<" to "/>", "<" to ">")
    }

    override fun getInnerHandler(): DelimiterHandler {
        return DelimiterHandler(isInner = true, sameLine = false, delimiterPairs = delimiters)
    }

    override fun getOuterHandler(): DelimiterHandler {
        return DelimiterHandler(isInner = false, sameLine = false, delimiterPairs = delimiters)
    }
}