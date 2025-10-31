package com.magidc.ideavim.anyobject.handlers

import com.magidc.ideavim.anyobject.handlers.base.AbstractPSIBasedHandler


class AnyFunctionHandler : AbstractPSIBasedHandler() {
    override fun getCommonSuffixes(): Set<String> = super.getCommonSuffixes() + setOf("DECLARATION", "DEFINITION")

    override val commonTypes: Set<String> = setOf("METHOD", "FUNCTION", "CONSTRUCTOR", "FUNCTION")

    override val languageSpecificTypes: Map<String, Set<String>> = mapOf(
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
