package com.magidc.ideavim.anyobject.handlers

/**
 * Handler for targeting any argument (in a function call) or any item (in a list, array, etc.).
 */
class AnyArgOrItemHandler : AnyItemHandler() {
    override val targetTypes = super.targetTypes + AnyArgumentHandler().targetTypes
    override val parentTargetTypes = super.parentTargetTypes + AnyArgumentHandler().parentTargetTypes
}
