package com.magidc.ideavim.anyobject.handlers


class AnyArgumentHandler : AnyItemHandler() {
    override val parentTypes = setOf(
        "arguments",
        "argument_list",
        "formal_parameters",
        "parameters"
    )
}
