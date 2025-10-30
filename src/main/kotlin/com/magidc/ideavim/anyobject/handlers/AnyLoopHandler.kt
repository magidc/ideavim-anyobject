package com.magidc.ideavim.anyobject.handlers

import com.magidc.ideavim.anyobject.handlers.base.AbstractPSIBasedHandler


class AnyLoopHandler : AbstractPSIBasedHandler() {
    companion object {
        private val commonLoopTypes = setOf("FOR", "WHILE", "DO", "DOWHILE", "FOREACH")
        private val languageLoopTypes = mapOf(
            "JAVA" to setOf("ENHANCEDFOR"),
            "JAVASCRIPT" to setOf("FORIN", "FOROF"),
            "ECMASCRIPT 6" to setOf("FORIN", "FOROF"),
            "DART" to setOf("FORIN"),
            "GO" to setOf("FORCLAUSE", "RANGECLAUSE"),
            "RUBY" to setOf("UNTIL"),
            "SWIFT" to setOf("REPEAT", "FORIN"),
            "RUST" to setOf("LOOP"),
            "PERL" to setOf("UNTIL"),
            "OBJECTIVE-C" to setOf("FORIN"),
            "HASKELL" to setOf("LISTCOMPREHENSION", "MONADCOMPREHENSION"),
            "F#" to setOf("FORLOOP", "WHILELOOP"),
            "GROOVY" to setOf("FORIN", "EACH"),
            "CLOJURE" to setOf("DOSEQ", "DOTIMES", "LOOP"),
            "LUA" to setOf("REPEAT", "FORNUMERIC", "FORGENERIC"),
            "CPP" to setOf("RANGEFOR"),
        )
    }

    override fun getSuffixes(): Set<String> = super.getSuffixes() + setOf("EXPR")

    override fun getCommonTypes(): Set<String> = commonLoopTypes

    override fun getLanguageSpecificTypes(): Map<String, Set<String>> = languageLoopTypes
}
