package com.magidc.ideavim.anyobject.handlers


class AnyArgumentHandlers : HandlerFactory {
    private class AnyArgument(isInner: Boolean) : AbstractPSIBasedHandler(isInner) {
        companion object {
            private val SUPPORTED_COLLECTION_TYPES = setOf("ARRAY_INITIALIZER_EXPRESSION", "PARAMETER_LIST", "EXPRESSION_LIST")
        }

        override fun acceptElementTypeName(elementName: String): Boolean {
            return SUPPORTED_COLLECTION_TYPES.contains(elementName)
        }
    }

    override fun getInnerHandler(): Handler {
        return AnyArgument(isInner = true)
    }

    override fun getOuterHandler(): Handler {
        return AnyArgument(isInner = false)
    }
}
