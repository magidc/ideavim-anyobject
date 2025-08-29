package com.magidc.ideavim.anyObject.handlers

import com.magidc.ideavim.anyobject.handlers.BaseHandlers
import com.magidc.ideavim.anyobject.handlers.DelimiterHandler

class AnyBracketHandlers : BaseHandlers {
    private val delimiters = listOf("[" to "]", "{" to "}", "(" to ")", "<" to "/>", "<" to ">")
    override fun getInnerHandler(): DelimiterHandler {
        return DelimiterHandler(true, false, delimiters)
    }

    override fun getOuterHandler(): DelimiterHandler {
        return DelimiterHandler(false, false, delimiters)
    }
}