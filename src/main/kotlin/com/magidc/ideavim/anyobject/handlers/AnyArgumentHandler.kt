package com.magidc.ideavim.anyobject.handlers

/**
 * Handler for targeting any argument or parameter in a function or method.
 */
class AnyArgumentHandler : AnyItemHandler() {
    override val parentTargetTypes = setOf(
        "arguments", "argument_list", "value_arguments", "formal_parameters", "formal_parameters_list",
        "parameters", "parameter_list", "function_parameters", "primary_constructor", "function_value_parameters",
        "lambda_parameters", "lambda_parameter_list", "array_creation_expression", "method_parameters", "attribute_argument_list"
    )
    override val targetTypes = setOf("argument", "parameter", "formal_parameter")
}
