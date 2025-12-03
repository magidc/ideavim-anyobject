package com.magidc.ideavim.anyobject.handlers

import com.magidc.ideavim.anyobject.handlers.base.AbstractTSBasedHandler
import org.treesitter.TSNode


class AnyConditionalHandler : AbstractTSBasedHandler() {
    companion object {
        private val innerBodyTypes = setOf("block", "switch_block_statement_group")
    }

    override val targetTypes = setOf("if_statement", "try_statement", "switch_expression")

    override fun findInnerBlock(node: TSNode?, offset: Int): TSNode? = node?.getFirstChildWithGrammar(innerBodyTypes, offset)
}
