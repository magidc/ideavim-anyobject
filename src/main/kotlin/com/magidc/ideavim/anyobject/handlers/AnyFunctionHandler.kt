package com.magidc.ideavim.anyobject.handlers

import com.magidc.ideavim.anyobject.handlers.base.AbstractTSBasedHandler

class AnyFunctionHandler : AbstractTSBasedHandler() {
    override val innerBlockTypes = setOf("block", "function_body", "compound_statement")
    override val targetTypes = setOf("method_declaration", "function_definition", "functions_declaration", "method_definition")
}
