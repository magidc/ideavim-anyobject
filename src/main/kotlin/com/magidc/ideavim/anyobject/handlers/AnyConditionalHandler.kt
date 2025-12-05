package com.magidc.ideavim.anyobject.handlers

import com.magidc.ideavim.anyobject.handlers.base.AbstractTSBasedHandler

class AnyConditionalHandler : AbstractTSBasedHandler() {
    override val innerBlockTypes = setOf("block", "switch_block_statement_group", "expression_statement", "case_statement", "compound_statement", "call_expression")
    override val targetTypes = setOf(
        "try_statement", "if_statement", "if_expression", "match_expression", "switch_statement", "switch_expression", "when_expression",
        "unless_statement", "branch_expression"
    )
}
