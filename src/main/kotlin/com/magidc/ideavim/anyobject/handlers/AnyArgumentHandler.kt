package com.magidc.ideavim.anyobject.handlers

class AnyArgumentHandler : AnyItemHandler() {
    override val targetTypes = super.targetTypes + setOf(
        "arguments", "argument_list", "value_arguments", "formal_parameters", "formal_parameters_list",
        "parameters", "parameter_list", "function_parameters", "primary_constructor", "function_value_parameters",
        "lambda_parameters", "lambda_parameter_list", "parameter_declaration", "formal_parameter", "set"
    )
}
