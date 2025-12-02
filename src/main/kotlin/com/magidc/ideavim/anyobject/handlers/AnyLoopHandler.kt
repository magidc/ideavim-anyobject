package com.magidc.ideavim.anyobject.handlers

import com.magidc.ideavim.anyobject.handlers.base.AbstractTSBasedHandler
import org.treesitter.TSNode


class AnyLoopHandler : AbstractTSBasedHandler() {
    companion object {
        private val targetTypes = setOf("for_statement", "while_statement", "do_statement")
    }

    override fun acceptNode(node: TSNode): Boolean {
        return !node.isNull && targetTypes.contains(node.grammarType)
    }
}
