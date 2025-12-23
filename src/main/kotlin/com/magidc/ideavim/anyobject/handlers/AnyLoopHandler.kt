package com.magidc.ideavim.anyobject.handlers

import com.magidc.ideavim.anyobject.handlers.base.TSBasedHandler


class AnyLoopHandler : TSBasedHandler() {
    // Keep it as a list to set an order of evaluation
    override val innerBlockTypes = listOf("block", "compound_statement", "expression_statement", "statements", "control_structure_body", "brace_list", "do", "statement_block")
    override val targetTypes = setOf(
        "do_statement", "do_while_expression", "do_while_statement",
        "enhanced_for_statement", "for", "for_expression", "for_in_statement", "for_range_loop", "for_statement", "foreach_statement",
        "loop_expression", "repeat", "repeat_while_statement", "until",
        "while", "while_expression", "while_statement"
    )
}
