package com.magidc.ideavim.anyobject.handlers

class AnyArgOrItemHandler : AnyItemHandler() {
    override val targetTypes = argumentTargetTypes + super.targetTypes
}

