package com.magidc.ideavim.anyobject.handlers

import com.intellij.psi.PsiElement
import com.magidc.ideavim.anyobject.handlers.base.AbstractPSIBasedHandler


class AnyConditionalHandler : AbstractPSIBasedHandler() {

    companion object {
        private val commonTypes = setOf("IF", "SWITCH", "CONDITIONAL", "TERNARY", "TRY", "TRYEXCEPT", "CASE")
        private val languageConditionalTypes = mapOf(
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
        )
    }

    override fun getCommonTypes(): Set<String> = commonTypes

    override fun getLanguageSpecificTypes(): Map<String, Set<String>> = languageConditionalTypes

    override fun getCodeBlockTypes(element: PsiElement): Set<String> {
        val language = element.language.id.uppercase()
        // Java and Kotlin may have inner code blocks in conditional statements without braces, therefore, is not always a CODE_BLOCK.
        if (language == "JAVA" || language == "KOTLIN") return super.getCodeBlockTypes(element) + "EXPRESSION_STATEMENT"
        return super.getCodeBlockTypes(element)
    }
}
