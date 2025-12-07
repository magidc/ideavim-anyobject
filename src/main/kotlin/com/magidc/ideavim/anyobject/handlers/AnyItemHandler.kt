package com.magidc.ideavim.anyobject.handlers

class AnyItemHandler : AnyArgumentHandler() {
    override val targetTypes = setOf("array", "array_initializer", "dictionary", "list", "tuple")
}

