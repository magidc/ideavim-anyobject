package com.magidc.ideavim.anyobject.handlers

import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.magidc.ideavim.anyobject.handlers.base.AbstractPSIBasedHandler
import com.magidc.ideavim.anyobject.handlers.base.BaseHandler
import com.magidc.ideavim.anyobject.handlers.base.HandlerFactory


class AnyFunctionHandlers : HandlerFactory {

    override fun getInnerHandler(): BaseHandler {
        return AnyFunctionHandler(isInner = true)
    }

    override fun getOuterHandler(): BaseHandler {
        return AnyFunctionHandler(isInner = false)
    }
}

private class AnyFunctionHandler(isInner: Boolean) : AbstractPSIBasedHandler(isInner) {
    companion object {
        private val languageFunctionTypes = mapOf(
            "JAVA" to setOf("METHOD"),
            "KOTLIN" to setOf("FUN"),
            "C#" to setOf("METHOD-DECLARATION", "CS:METHOD-DECLARATION"),
            "PYTHON" to setOf("FUNCTION_DECLARATION", "PYFUNCTION"),
            "JAVASCRIPT" to setOf("FUNCTION_DECLARATION", "FUNCTION", "FUNCTION_EXPRESSION", "ARROW_FUNCTION"),
            "ECMASCRIPT 6" to setOf("FUNCTION_DECLARATION", "FUNCTION", "FUNCTION_EXPRESSION", "ARROW_FUNCTION"),
            "TYPESCRIPT" to setOf("JS:TYPESCRIPT_FUNCTION", "FUNCTION_DECLARATION", "FUNCTION", "ARROW_FUNCTION", "METHOD_SIGNATURE"),
            "DART" to setOf("FUNCTION_DECLARATION", "METHOD_DECLARATION"),
            "GO" to setOf("FUNCTION_DECLARATION", "METHOD_DECLARATION", "FUNC_DECLARATION"),
            "PERL" to setOf("SUB_DEFINITION", "PERL5:SUB_DEFINITION"),
            "RUBY" to setOf("RUBY:METHOD", "RUBY:FUNCTION"),
            "SCALA" to setOf("FUNCTION DEFINITION", "METHOD_DEFINITION", "DEF_DEFINITION"),
            "PHP" to setOf("FUNCTION", "CLASS_METHOD", "FUNCTION_DECLARATION", "METHOD_DECLARATION"),
            "R" to setOf("R_FUNCTION_EXPRESSION", "R_FUNCTION_DEFINITION"),
            "RUST" to setOf("FUNCTION", "FN", "FUNCTION_ITEM", "ASSOCIATED_FUNCTION"),
            "HASKELL" to setOf("HS:FUNCTION_DECLARATION", "HS:BINDING", "HS:FUNCTION_DEFINITION"),
            "F#" to setOf("FS:FUNCTION_DEFINITION", "FS:METHOD_DEFINITION", "FS:MEMBER_DEFINITION"),
            "GROOVY" to setOf("METHOD", "FUNCTION", "CLOSURE_EXPRESSION"),
            "CLOJURE" to setOf("FUNCTION", "DEFN", "FN"),
            "LUA" to setOf("FUNCTION", "FUNCTION_DECLARATION", "LOCAL_FUNCTION"),
            "CPP" to setOf("FUNCTION_DECLARATION", "FUNCTION_DEFINITION", "METHOD_DECLARATION"),
            "C" to setOf("FUNCTION_DECLARATION", "FUNCTION_DEFINITION")
        )
    }

    override fun acceptElement(element: PsiElement, language: String): Boolean {
        val elementTypeName = element.elementType.toString().uppercase()
        val functionTypes = languageFunctionTypes[language] ?: emptySet()
        
        return functionTypes.any { functionType ->
            when {
                functionType.contains("-") -> elementTypeName.endsWith(functionType)
                functionType.contains(":") -> elementTypeName == functionType
                functionType.contains("_") -> elementTypeName.endsWith(functionType) || elementTypeName.startsWith(functionType)
                else -> elementTypeName == functionType || elementTypeName.endsWith(functionType)
            }
        }
    }
}
