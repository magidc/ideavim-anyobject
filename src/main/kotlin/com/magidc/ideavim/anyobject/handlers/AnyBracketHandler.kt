package com.magidc.ideavim.anyobject.handlers

import com.magidc.ideavim.anyobject.handlers.base.DelimiterHandler

class AnyBracketHandler : DelimiterHandler(false, delimiters) {
    companion object {
        private val delimiters = listOf("(" to ")", "[" to "]", "{" to "}", "<" to "/>", "<" to ">")
    }
}