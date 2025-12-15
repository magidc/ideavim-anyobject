package com.magidc.ideavim.anyobject.handlers

import com.maddyhome.idea.vim.common.TextRange
import com.magidc.ideavim.anyobject.handlers.base.AbstractTSBasedHandler
import com.magidc.ideavim.anyobject.utils.TSDocument
import com.magidc.ideavim.anyobject.utils.TSModelExtensions.Companion.toText
import org.treesitter.TSNode

class AnyStringHandler : AbstractTSBasedHandler() {
    companion object {
        private val quotes = setOf('"', '\'', '`')
    }

    override val targetTypes = setOf("string", "string_literal", "verbatim_string_literal", "interpolated_string_expression", "raw_string_literal")

    override fun findInnerBlockRange(node: TSNode, offset: Int, tsDocument: TSDocument): TextRange? {
        val string = node.toText(tsDocument.editor, maxSize = Int.MAX_VALUE)
        if (string.isEmpty()) return null
        val prefix = string.takeWhile { it !in quotes }
        val startQuotes = string.substring(prefix.length).takeWhile { it in quotes }
        val endQuotes = string.takeLastWhile { it in quotes }
        return TextRange(
            tsDocument.toCharOffset(node.startByte) + prefix.length + startQuotes.length,
            tsDocument.toCharOffset(node.endByte) - endQuotes.length
        )
    }
}
