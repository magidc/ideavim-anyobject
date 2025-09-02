package com.magidc.ideavim.anyobject.handlers

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