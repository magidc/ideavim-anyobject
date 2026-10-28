package com.magidc.ideavim.anyobject.handlers

import com.magidc.ideavim.anyobject.handlers.base.TSBasedHandler

/**
 * Handler for targeting any function call expression
 */
class AnyCallHandler : TSBasedHandler() {

    override val innerBlockTypes = AnyArgumentHandler.parentTargetTypes

    override val targetTypes = setOf("call_expression", "method_invocation")
}
