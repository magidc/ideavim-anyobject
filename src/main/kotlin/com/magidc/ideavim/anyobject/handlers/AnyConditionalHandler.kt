package com.magidc.ideavim.anyobject.handlers

import com.intellij.psi.PsiElement
import com.magidc.ideavim.anyobject.handlers.base.AbstractPSIBasedHandler


class AnyConditionalHandler : AbstractPSIBasedHandler() {
    override val commonTypes: Set<String> = setOf("IF", "SWITCH", "CONDITIONAL", "TERNARY", "TRY", "TRYEXCEPT", "CASE")
    override val languageSpecificTypes: Map<String, Set<String>> = mapOf(
        "KOTLIN" to setOf("WHEN", "ELVIS"),
        "GO" to setOf("SELECT"),
        "PHP" to setOf("MATCH"),
        "RUBY" to setOf("UNLESS", "BEGIN"),
        "SWIFT" to setOf("GUARD", "DO_CATCH"),
        "RUST" to setOf("IFEXPR", "MATCHEXPR", "IFLETEXPR", "MATCHARM"),
        "PERL" to setOf("UNLESS", "GIVEN", "EVAL"),
        "OBJECTIVE-C" to setOf("@TRY"),
        "HASKELL" to setOf("GUARD"),
        "GROOVY" to setOf("ELVIS"),
        "CLOJURE" to setOf("COND", "WHEN", "IFNOT"),
        "LUA" to setOf("ELSEIF"),
        "C#" to setOf("IFELSEKOPS", "SWITCHKOPS", "TRYCATCHFINALLYKOPS"),
    )

    override fun getCodeBlockTypes(element: PsiElement): Set<String> {
        val language = getLanguage(element)
        // Java and Kotlin may have inner code blocks in conditional statements without braces, therefore, is not always a CODE_BLOCK.
        if (language == "JAVA" || language == "KOTLIN") return super.getCodeBlockTypes(element) + "EXPRESSION_STATEMENT"
        return super.getCodeBlockTypes(element)
    }
}
