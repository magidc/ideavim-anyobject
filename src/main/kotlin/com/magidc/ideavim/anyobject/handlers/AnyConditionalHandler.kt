package com.magidc.ideavim.anyobject.handlers

import com.magidc.ideavim.anyobject.handlers.base.TSBasedHandler

class AnyConditionalHandler : TSBasedHandler() {

    override val innerBlockTypes = setOf(
        "switch_block_statement_group", "switch_section", "case_statement", "block", "else",
        "return_statement", "when_entry", "match_arm", "default_statement", "case_clause", "else_clause", "braced_expression",
        "statement_block", "switch_case", "then", "switch_default", "statements", "switch_entry", "default_case", "expression_case"
    )

    override val targetTypes = setOf(
        "match_statement", "match_expression", "switch_statement", "switch_expression", "when_expression", "begin", "case", "try_expression",
        "try_statement", "if_statement", "if_expression", "unless_statement", "branch_expression", "try_with_resources_statement", "do_statement",
        "expression_switch_statement", "if"
    )
}
