package com.magidc.ideavim.anyobject.handlers

import com.magidc.ideavim.anyobject.handlers.base.AbstractTSBasedHandler
import org.treesitter.TSNode


open class AnyItemHandler : AbstractTSBasedHandler() {
    protected open val parentTypes = setOf("argument", "simple_parameter")

    override fun acceptNode(node: TSNode): Boolean {
        return !node.parent.isNull && parentTypes.contains(node.parent.grammarType)
    }

    override fun allowsCountSelection(): Boolean = true

}

