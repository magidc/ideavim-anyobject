package com.magidc.ideavim.anyobject.handlers

import com.magidc.ideavim.anyobject.handlers.base.TSBasedHandler

open class AnyFunctionHandler : TSBasedHandler() {
    override val innerBlockTypes =
        setOf(
            "block", "function_body", "compound_statement", "body_statement", "statements", "binary_operator", "binary_expression",
            "statement_block", "infix_expression", "expression_statement", "brace_list", "binary"
        )
    override val targetTypes = setOf(
        "anonymous_initializer", "block_expression", "constructor_declaration",
        "defn_def", "delegate_declaration", "fn_declaration", "function_declaration", "function_definition", "function_item", "function_specification",
        "function_statement", "generator_function_declaration", "init_block", "local_function_declaration",
        "method", "method_declaration", "method_definition", "method_signature", "procedure_definition", "subroutine_declaration"
    )
}
