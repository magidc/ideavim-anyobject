package com.magidc.ideavim.anyobject.handlers

import com.magidc.ideavim.anyobject.handlers.base.AbstractPSIBasedHandler


class AnyFunctionHandler : AbstractPSIBasedHandler() {
    companion object {
        private val commonFunctionTypes = setOf("METHOD", "FUNCTION", "CONSTRUCTOR", "FUNCTION")
        private val languageFunctionTypes = mapOf(
            "KOTLIN" to setOf("FUN"),
            "JAVASCRIPT" to setOf("ARROWFUNCTION"),
            "ECMASCRIPT 6" to setOf("ARROWFUNCTION"),
            "TYPESCRIPT" to setOf("TYPESCRIPTFUNCTION", "ARROWFUNCTION", "METHODSIGNATURE"),
            "GO" to setOf("FUNC"),
            "PERL" to setOf("SUB"),
            "SCALA" to setOf("DEF"),
            "PHP" to setOf("CLASSMETHOD"),
            "SWIFT" to setOf("INIT", "SUBSCRIPT"),
            "RUST" to setOf("FN", "FUNCTIONITEM", "ASSOCIATEDFUNCTION"),
            "HASKELL" to setOf("BINDING"),
            "F#" to setOf("MEMBER"),
            "GROOVY" to setOf("CLOSURE"),
            "CLOJURE" to setOf("DEFN", "FN"),
            "LUA" to setOf("LOCALFUNCTION", "ANONYMOUSFUNCTION"),
        )
    }

    override fun getCommonTypes(): Set<String> = commonFunctionTypes

    override fun getLanguageSpecificTypes(): Map<String, Set<String>> = languageFunctionTypes

    override fun getSuffixes(): Set<String> = super.getSuffixes() + setOf("DECLARATION", "DEFINITION")
}
