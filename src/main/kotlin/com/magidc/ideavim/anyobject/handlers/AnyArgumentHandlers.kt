package com.magidc.ideavim.anyobject.handlers


class AnyArgumentHandlers : HandlerFactory {
    private class AnyArgument(isInner: Boolean) : BaseItemHandler(isInner) {
        companion object {
            private val SUPPORTED_COLLECTION_TYPES = setOf("ARRAY_INITIALIZER_EXPRESSION", "PARAMETER_LIST", "EXPRESSION_LIST")
        }

        override fun acceptElementTypeName(elementName: String): Boolean {
            return SUPPORTED_COLLECTION_TYPES.contains(elementName)
        }
    }

    override fun getInnerHandler(): BaseHandler {
        return AnyArgument(isInner = true)
    }

    override fun getOuterHandler(): BaseHandler {
        return AnyArgument(isInner = false)
    }
}
