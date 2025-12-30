package com.magidc.ideavim.anyobject.handlers

class AnyArgOrItemHandler : AnyItemHandler() {
    override val targetTypes = super.targetTypes + AnyArgumentHandler().targetTypes
    override val parentTargetTypes = super.parentTargetTypes + AnyArgumentHandler().parentTargetTypes
}
