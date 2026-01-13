package com.magidc.ideavim.anyobject.handlers

/**
 * Handler for targeting any lambda expression or function definition.
 */
class AnyLambdaOrFunctionHandler : AnyFunctionHandler() {
    override val innerBlockTypes = super.innerBlockTypes + setOf("method_invocation", "arrow_expression_clause")
    override val targetTypes = super.targetTypes + setOf(
        "anonymous_function", "anonymous_function_expression", "arrow_function", "closure_expression",
        "do_block", "func_literal", "lambda", "lambda_expression", "lambda_literal"
    )
}
