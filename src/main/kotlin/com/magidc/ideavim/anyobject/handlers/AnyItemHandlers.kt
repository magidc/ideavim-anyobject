package com.magidc.ideavim.anyobject.handlers


class AnyItemHandlers : HandlerFactory {
    private class AnyItemHandler(isInner: Boolean) : AbstractPSIBasedHandler(isInner) {
        companion object {
            private val SUPPORTED_COLLECTION_TYPES = setOf("ARRAY_INITIALIZER_EXPRESSION", "FOR_STATEMENT")
        }

        override fun acceptElementTypeName(elementName: String): Boolean {
            return elementName.lowercase().endsWith("list") || SUPPORTED_COLLECTION_TYPES.contains(elementName)
        }
    }

    override fun getInnerHandler(): Handler {
        return AnyItemHandler(isInner = true)
    }

    override fun getOuterHandler(): Handler {
        return AnyItemHandler(isInner = false)
    }
}