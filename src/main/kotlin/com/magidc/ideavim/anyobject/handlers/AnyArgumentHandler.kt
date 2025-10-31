package com.magidc.ideavim.anyobject.handlers


class AnyArgumentHandler : AnyItemHandler() {
    override fun getCommonSuffixes(): Set<String> = setOf("LIST")
}
