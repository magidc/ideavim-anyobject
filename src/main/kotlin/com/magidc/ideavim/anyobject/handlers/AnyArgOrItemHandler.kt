package com.magidc.ideavim.anyobject.handlers

/**
 * Handler for targeting any argument (in a function call) or any item (in a list, array, etc.).
 */
class AnyArgOrItemHandler : AnyArgumentHandler() {
    override val targetTypes = super.targetTypes + AnyItemHandler.targetTypes
    override val parentTargetTypes = super.parentTargetTypes + AnyItemHandler.parentTargetTypes

}
