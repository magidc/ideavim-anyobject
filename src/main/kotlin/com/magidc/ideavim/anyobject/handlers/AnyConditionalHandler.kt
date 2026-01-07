package com.magidc.ideavim.anyobject.handlers

import com.maddyhome.idea.vim.common.TextRange
import com.magidc.ideavim.anyobject.handlers.base.TSBasedHandler
import com.magidc.ideavim.anyobject.utils.TSDocument
import org.treesitter.TSNode

class AnyConditionalHandler : TSBasedHandler() {

    override val innerBlockTypes = setOf(
        "switch_block_statement_group", "switch_section", "case_statement", "block",
        "return_statement", "control_structure_body", "when_entry", "match_arm", "default_statement", "else_clause", "case_clause",
        "compound_statement", "statement_block", "switch_case", "then"
    )

    override val targetTypes = setOf(
        "match_statement", "switch_statement", "switch_expression", "when_expression", "begin", "case",
        "try_statement", "if_statement", "if_expression", "unless_statement", "branch_expression", "try_with_resources_statement"
    )

    override fun findInnerBlockRange(currentNode: TSNode, objectNode: TSNode, offset: Int, tsDocument: TSDocument): TextRange {
        return generateSequence(currentNode) { it.parent.takeIf { n -> !n.isNull } }
            .firstOrNull { n -> innerBlockTypes.any { n.grammarType == it } }
            ?.let { getCodeBlock(it, tsDocument) }
            ?: tsDocument.toTextRange(objectNode)
    }
}
