package com.magidc.ideavim.anyobject.handlers

import com.magidc.ideavim.anyobject.handlers.base.AbstractTSBasedHandler
import org.treesitter.TSNode


class AnyClassHandler : AbstractTSBasedHandler() {
    companion object {
        private val innerBodyTypes = setOf("class_body", "interface_body")
    }

    override val targetTypes: Set<String> = setOf("class_declaration", "class_definition", "interface_declaration", "record_declaration")

    override fun findInnerBlock(node: TSNode?, offset: Int): TSNode? = node?.getFirstChildWithGrammar(innerBodyTypes, offset)

}