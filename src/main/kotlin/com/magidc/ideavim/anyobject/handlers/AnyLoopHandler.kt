package com.magidc.ideavim.anyobject.handlers

import com.magidc.ideavim.anyobject.handlers.base.TSBasedHandler


class AnyLoopHandler : TSBasedHandler() {
    // Keep it as a list to set an order of evaluation
    override val innerBlockTypes = listOf("block", "compound_statement", "expression_statement", "statements", "control_structure_body")
    override val targetTypes = setOf(
        "for_statement", "enhanced_for_statement", "while_statement", "do_statement",
        "for_each_statement", "foreach_statement", "for_range_statement", "range_for_statement",
        "indexed_for_statement", "for_of_statement", "for_in_statement", "while_expression",
        "do_while_statement", "do_until_statement", "async_for_statement", "loop_expression",
        "for_expression", "while_expression", "do_while_expression", "until_expression",
        "for_in_expression"
    )
}
