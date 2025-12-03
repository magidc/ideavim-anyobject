package com.magidc.ideavim.anyobject.handlers

import com.magidc.ideavim.anyobject.handlers.base.AbstractTSBasedHandler


class AnyFunctionHandler : AbstractTSBasedHandler() {
    override val targetTypes = setOf("method_declaration", "function_definition")
}
