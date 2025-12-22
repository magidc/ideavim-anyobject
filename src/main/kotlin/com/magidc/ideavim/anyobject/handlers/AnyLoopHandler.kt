package com.magidc.ideavim.anyobject.handlers

import com.magidc.ideavim.anyobject.handlers.base.TSBasedHandler


class AnyLoopHandler : TSBasedHandler() {
    // Keep it as a list to set an order of evaluation
    override val innerBlockTypes = listOf("block", "compound_statement", "expression_statement", "statements", "control_structure_body", "brace_list", "do", "statement_block")
    override val targetTypes = setOf(
        "until", "while", "for", "repeat", "for_statement", "enhanced_for_statement", "while_statement", "do_statement",
        "for_range_loop", "foreach_statement", "for_in_statement", "while_expression", "do_while_statement", "loop_expression",
        "for_expression", "while_expression", "do_while_expression", "repeat_while_statement"
    )
}
