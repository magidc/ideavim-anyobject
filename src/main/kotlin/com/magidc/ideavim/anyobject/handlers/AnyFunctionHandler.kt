package com.magidc.ideavim.anyobject.handlers

import com.magidc.ideavim.anyobject.handlers.base.AbstractTSBasedHandler
import org.treesitter.TSNode


class AnyFunctionHandler : AbstractTSBasedHandler() {
    companion object {
        private val targetTypes = setOf("method_declaration", "function_definition")
    }

    override fun acceptNode(node: TSNode): Boolean {
        return !node.isNull && targetTypes.contains(node.grammarType)
    }
}
