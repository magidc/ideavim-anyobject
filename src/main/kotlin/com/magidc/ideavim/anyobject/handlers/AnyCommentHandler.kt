package com.magidc.ideavim.anyobject.handlers

import com.magidc.ideavim.anyobject.handlers.base.AbstractTSBasedHandler
import org.treesitter.TSNode

class AnyCommentHandler : AbstractTSBasedHandler() {
    companion object {
        private val targetTypes = setOf("comment", "line_comment", "block_comment")
    }

    override fun acceptNode(node: TSNode): Boolean {
        return !node.isNull && targetTypes.contains(node.grammarType)
    }
}