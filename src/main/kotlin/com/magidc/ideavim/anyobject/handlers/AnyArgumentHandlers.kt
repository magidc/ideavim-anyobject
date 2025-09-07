package com.magidc.ideavim.anyobject.handlers


class AnyArgumentHandlers : HandlerFactory {
    private class AnyArgument(isInner: Boolean) : AbstractPSIBasedHandler(
        isInner,
        setOf("ARRAY_INITIALIZER_EXPRESSION", "PARAMETER_LIST", "EXPRESSION_LIST", "STATEMENTTLIST", "ARGUMENT_LIST")
    )

    override fun getInnerHandler(): Handler {
        return AnyArgument(isInner = true)
    }

    override fun getOuterHandler(): Handler {
        return AnyArgument(isInner = false)
    }
}
