package com.magidc.ideavim.anyobject.handlers

import org.treesitter.TSNode

class AnyItemHandler : AnyArgumentHandler() {
    override val targetTypes = setOf("array", "array_initializer", "list", "tuple")

    override fun acceptNode(node: TSNode): Boolean = super.acceptNode(node) || node.grammarType == "pair"
}

