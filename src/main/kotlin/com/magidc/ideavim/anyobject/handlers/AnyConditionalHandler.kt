package com.magidc.ideavim.anyobject.handlers

import com.maddyhome.idea.vim.common.TextRange
import com.magidc.ideavim.anyobject.handlers.base.AbstractTSBasedHandler
import com.magidc.ideavim.anyobject.utils.TSDocument
import com.magidc.ideavim.anyobject.utils.TSModelExtensions.Companion.getFirstNamedChildWithGrammar
import org.treesitter.TSNode

class AnyConditionalHandler : AbstractTSBasedHandler() {
    override val innerBlockTypes = listOf(
        "block", "switch_block_statement_group", "expression_statement", "switch_section",
        "compound_statement", "call_expression", "return_statement", "control_structure_body"
    )
    override val targetTypes = setOf(
        "try_statement", "if_statement", "if_expression", "match_expression", "switch_statement", "switch_expression", "when_expression",
        "unless_statement", "branch_expression", "try_with_resources_statement"
    )

    override fun findInnerBlockRange(node: TSNode, offset: Int, tsDocument: TSDocument): TextRange? {
        if (node.grammarType == "switch_statement")
            return node.getFirstNamedChildWithGrammar(setOf("case_statement"), offset)?.let { tsDocument.toTextRange(it) }
        return super.findInnerBlockRange(node, offset, tsDocument)
    }
}
