package com.magidc.ideavim.anyobject.handlers

import com.magidc.ideavim.anyobject.handlers.base.TSBasedHandler

class AnyConditionalHandler : TSBasedHandler() {

    override val innerBlockTypes = setOf(
        "switch_block_statement_group", "switch_section", "block", "case_statement", "else_clause",
        "return_statement", "control_structure_body", "when_entry", "match_arm", "default_statement", "case_clause"

    )
    override val targetTypes = setOf(
        "match_statement", "switch_statement", "switch_expression", "when_expression",
        "try_statement", "if_statement", "if_expression", "unless_statement", "branch_expression", "try_with_resources_statement"
    )

}
