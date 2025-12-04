package com.magidc.ideavim.anyobject.handlers

import com.magidc.ideavim.anyobject.handlers.base.AbstractTSBasedHandler

class AnyConditionalHandler : AbstractTSBasedHandler() {
    override val targetTypes = setOf("if_statement", "try_statement", "switch_expression", "switch_statement")
    override val innerBlockTypes = setOf("block", "switch_block_statement_group", "expression_statement", "case_statement", "compound_statement", "call_expression")
}
