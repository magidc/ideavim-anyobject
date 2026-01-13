package com.magidc.ideavim.anyobject.handlers

import com.magidc.ideavim.anyobject.handlers.base.DelimiterHandler

/**
 * Handler for targeting the content within any type of quotes: ", ', `.
 */
class AnyQuoteHandler : DelimiterHandler(true,delimiters) {
    companion object {
        private val delimiters = listOf("\"" to "\"", "'" to "'", "`" to "`")
    }
}