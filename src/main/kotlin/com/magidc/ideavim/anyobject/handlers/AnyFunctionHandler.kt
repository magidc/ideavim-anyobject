package com.magidc.ideavim.anyobject.handlers

import com.magidc.ideavim.anyobject.handlers.base.AbstractTSBasedHandler

class AnyFunctionHandler : AbstractTSBasedHandler() {
    override val innerBlockTypes = setOf("block", "function_body", "compound_statement")
    override val targetTypes = setOf(
        "method_declaration", "function_definition", "function_declaration", "method_definition", "constructor_declaration",
        "function_statement", "lambda_expression", "arrow_function", "anonymous_function", "closure_expression", "fn_declaration",
        "function_specification", "procedure_definition", "subroutine_declaration",
    )
}
