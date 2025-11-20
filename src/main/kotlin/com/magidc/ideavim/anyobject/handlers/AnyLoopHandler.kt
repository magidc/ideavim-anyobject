package com.magidc.ideavim.anyobject.handlers

import com.magidc.ideavim.anyobject.handlers.base.AbstractPSIBasedHandler


class AnyLoopHandler : AbstractPSIBasedHandler() {

    override fun getCommonSuffixes(): Set<String> = super.getCommonSuffixes() + setOf("EXPR")

    override val commonTypes: Set<String> = setOf("FOR", "WHILE", "DO", "DOWHILE", "FOREACH")

    override val languageSpecificTypes: Map<String, Set<String>> = mapOf(
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
        "C#" to setOf("WHILEKOPS", "FOREACHKOPS", "FORKOPS", "DOWHILEKOPS"),
        "GROOVY" to setOf("FORIN", "EACH"),
        "CLOJURE" to setOf("DOSEQ", "DOTIMES", "LOOP"),
        "LUA" to setOf("REPEAT", "FORNUMERIC", "FORGENERIC"),
        "CPP" to setOf("RANGEFOR"),
    )
}
