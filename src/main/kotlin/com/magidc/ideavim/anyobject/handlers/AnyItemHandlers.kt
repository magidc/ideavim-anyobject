package com.magidc.ideavim.anyobject.handlers


class AnyItemHandlers : HandlerFactory {
    private class AnyItemHandler(isInner: Boolean) : AbstractPSIBasedHandler(
        isInner,
        setOf("LIST", "ARRAY_INITIALIZER_EXPRESSION", "FOR_STATEMENT")
    )

    override fun getInnerHandler(): Handler {
        return AnyItemHandler(isInner = true)
    }

    override fun getOuterHandler(): Handler {
        return AnyItemHandler(isInner = false)
    }
}