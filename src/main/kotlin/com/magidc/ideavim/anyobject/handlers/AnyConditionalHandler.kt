package com.magidc.ideavim.anyobject.handlers

import com.magidc.ideavim.anyobject.handlers.base.AbstractTSBasedHandler
import org.treesitter.TSNode


class AnyConditionalHandler : AbstractTSBasedHandler() {
    companion object {
        private val targetTypes = setOf("if_statement", "try_statement", "switch_expression")
    }

    override fun acceptNode(node: TSNode): Boolean {
        return !node.isNull && targetTypes.contains(node.grammarType)
    }
}
