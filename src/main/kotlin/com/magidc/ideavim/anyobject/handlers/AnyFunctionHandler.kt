package com.magidc.ideavim.anyobject.handlers

import com.magidc.ideavim.anyobject.handlers.base.TSBasedHandler

class AnyFunctionHandler : TSBasedHandler() {
    // Keep it as a list to set an order of evaluation
    override val innerBlockTypes =
        listOf(
            "block", "function_body", "compound_statement", "body_statement", "method_invocation", "statements", "binary_operator", "binary_expression", "arrow_expression_clause",
            "statement_block", "infix_expression", "expression_statement", "brace_list", "binary"
        )
    override val targetTypes = setOf(
        "anonymous_function", "anonymous_function_expression", "anonymous_initializer", "arrow_function", "block_expression", "closure_expression", "constructor_declaration",
        "defn_def", "delegate_declaration", "do_block", "fn_declaration", "func_literal", "function_declaration", "function_definition", "function_item", "function_specification",
        "function_statement", "generator_function_declaration", "init_block", "lambda", "lambda_expression", "lambda_literal", "local_function_declaration",
        "method", "method_declaration", "method_definition", "method_signature", "procedure_definition", "subroutine_declaration"
    )
}
