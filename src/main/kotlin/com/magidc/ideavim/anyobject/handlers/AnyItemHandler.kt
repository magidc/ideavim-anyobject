package com.magidc.ideavim.anyobject.handlers

import org.treesitter.TSNode

open class AnyItemHandler : AnyArgumentHandler() {
    override val targetTypes: Set<String> = setOf("array", "array_initializer", "list", "tuple", "initializer_expression")

    override fun acceptNode(node: TSNode): Boolean = super.acceptNode(node) || node.grammarType == "pair"
}

