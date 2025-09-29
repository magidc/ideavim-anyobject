package com.magidc.ideavim.anyobject.handlers

import com.magidc.ideavim.anyobject.handlers.base.DelimiterHandler

class AnyQuoteHandler : DelimiterHandler(true,delimiters) {
    companion object {
        private val delimiters = listOf("\"" to "\"", "'" to "'", "`" to "`")
    }
}