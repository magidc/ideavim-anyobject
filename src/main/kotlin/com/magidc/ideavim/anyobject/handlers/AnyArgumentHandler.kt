package com.magidc.ideavim.anyobject.handlers


class AnyArgumentHandler : AnyItemHandler() {
    companion object {
        private val commonArgumentTypes = setOf("PARAMETER", "ARGUMENT", "EXPRESSION")
        private val languageArgumentTypes = mapOf(
            "KOTLIN" to setOf("VALUEARGUMENT", "TYPEPARAMETER"),
            "JAVASCRIPT" to setOf("FORMALPARAMETER"),
            "ECMASCRIPT 6" to setOf("FORMALPARAMETER"),
            "TYPESCRIPT" to setOf("TYPEPARAMETER", "FORMALPARAMETER"),
            "DART" to setOf("FORMALPARAMETER", "TYPEPARAMETER"),
            "GO" to setOf("FIELD", "PARAMETERDECLARATION"),
            "PHP" to setOf("FORMALPARAMETER"),
            "SCALA" to setOf("PARAMETERCLAUSE", "TYPEPARAMETER"),
            "SWIFT" to setOf("FUNCTIONCALLARGUMENT", "PARAMETERCLAUSE"),
            "RUST" to setOf("VALUEPARAMETER", "TYPEPARAMETER"),
            "CLOJURE" to setOf("VECTOR", ""),
            "LUA" to setOf("FUNCTIONCALL"),
        )
    }

    override fun getCommonTypes(): Set<String> = commonArgumentTypes

    override fun getLanguageSpecificTypes(): Map<String, Set<String>> = languageArgumentTypes

    override fun getSuffixes(): Set<String> = setOf("LIST")
}
