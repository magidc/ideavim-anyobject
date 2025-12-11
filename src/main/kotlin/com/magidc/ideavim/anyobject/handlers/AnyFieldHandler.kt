package com.magidc.ideavim.anyobject.handlers

import com.magidc.ideavim.anyobject.handlers.base.AbstractTSBasedHandler
import org.treesitter.TSNode

class AnyFieldHandler : AbstractTSBasedHandler() {
    override val targetTypes = setOf(
        "property_declaration", "field_declaration", "field_definition", "local_declaration_statement", "local_variable_declaration", "declaration"
    )

    override fun findInnerBlock(node: TSNode?, offset: Int): TSNode? {
        if (node == null) return null
        return node.getFirstChildWithGrammar(setOf("="))?.nextChild()?.takeIf { !it.isNull } ?: node
    }
}