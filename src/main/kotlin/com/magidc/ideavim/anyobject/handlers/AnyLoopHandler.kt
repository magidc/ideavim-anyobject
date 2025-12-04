package com.magidc.ideavim.anyobject.handlers

import com.magidc.ideavim.anyobject.handlers.base.AbstractTSBasedHandler

class AnyLoopHandler : AbstractTSBasedHandler() {
    override val innerBlockTypes = setOf("block", "compound_statement")
    override val targetTypes = setOf("for_statement", "while_statement", "do_statement")
}
