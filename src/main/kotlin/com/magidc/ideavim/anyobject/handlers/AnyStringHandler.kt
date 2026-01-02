package com.magidc.ideavim.anyobject.handlers

import com.maddyhome.idea.vim.common.TextRange
import com.magidc.ideavim.anyobject.handlers.base.TSBasedHandler
import com.magidc.ideavim.anyobject.utils.TSDocument
import com.magidc.ideavim.anyobject.utils.TSModelExtensions.Companion.toText
import org.treesitter.TSNode

class AnyStringHandler : TSBasedHandler() {
    companion object {
        private val quotes = setOf('"', '\'', '`')
    }

    override val targetTypes = setOf(
        "string", "string_literal", "verbatim_string_literal", "interpolated_string_expression", "raw_string_literal",
        "encapsed_string", "single_quote_scalar", "double_quote_scalar", "line_string_literal", "multi_line_string_literal",
        "interpreted_string_literal", "template_string"
    )

    override fun findInnerBlockRange(node: TSNode, offset: Int, tsDocument: TSDocument): TextRange? {
        val string = node.toText(tsDocument.editor, maxSize = Int.MAX_VALUE)
        if (string.isEmpty()) return null
        val prefix = string.takeWhile { it !in quotes }
        val startQuotes = string.substring(prefix.length).takeWhile { it in quotes }
        if (startQuotes.isEmpty()) return tsDocument.toTextRange(node)
        val innerQuoteChar = startQuotes.last()
        val endQuotes = string.substring(prefix.length + startQuotes.length).substringAfter(innerQuoteChar) + "x"

        return TextRange(
            tsDocument.toCharOffset(node.startByte) + prefix.length + startQuotes.length,
            tsDocument.toCharOffset(node.endByte) - endQuotes.length
        )
    }
}
