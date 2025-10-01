package com.magidc.ideavim.anyobject.handlers

import com.intellij.psi.PsiElement


class AnyArgumentHandler : AnyItemHandler() {
    companion object {
        private val languageArgumentTypes = mapOf(
            "JAVA" to setOf("PARAMETER_LIST", "EXPRESSION_LIST", "ARGUMENT_LIST"),
            "KOTLIN" to setOf("VALUE_ARGUMENT_LIST", "PARAMETER_LIST", "TYPE_PARAMETER_LIST"),
            "PYTHON" to setOf("PARAMETER_LIST", "ARGUMENT_LIST", "PYARGUMENTLIST"),
            "JAVASCRIPT" to setOf("PARAMETER_LIST", "ARGUMENT_LIST", "EXPRESSION_LIST", "FORMAL_PARAMETER_LIST"),
            "ECMASCRIPT 6" to setOf("PARAMETER_LIST", "ARGUMENT_LIST", "EXPRESSION_LIST", "FORMAL_PARAMETER_LIST"),
            "TYPESCRIPT" to setOf("PARAMETER_LIST", "ARGUMENT_LIST", "EXPRESSION_LIST", "TYPE_PARAMETER_LIST", "FORMAL_PARAMETER_LIST"),
            "C#" to setOf("PARAMETER_LIST", "ARGUMENT_LIST", "CS:PARAMETER-LIST", "CS:ARGUMENT-LIST"),
            "DART" to setOf("PARAMETER_LIST", "ARGUMENT_LIST", "FORMAL_PARAMETER_LIST", "TYPE_PARAMETER_LIST"),
            "GO" to setOf("PARAMETER_LIST", "ARGUMENT_LIST", "FIELD_LIST", "PARAMETER_DECLARATION_LIST"),
            "PHP" to setOf("PARAMETER_LIST", "ARGUMENT_LIST", "FORMAL_PARAMETER_LIST"),
            "RUBY" to setOf("PARAMETER_LIST", "ARGUMENT_LIST", "RUBY:ARGUMENT_LIST", "RUBY:PARAMETER_LIST"),
            "SCALA" to setOf("PARAMETER_LIST", "ARGUMENT_LIST", "PARAMETER_CLAUSE", "TYPE_PARAMETER_LIST"),
            "SWIFT" to setOf("PARAMETER_LIST", "ARGUMENT_LIST", "FUNCTION_CALL_ARGUMENT_LIST", "PARAMETER_CLAUSE"),
            "RUST" to setOf("PARAMETER_LIST", "ARGUMENT_LIST", "VALUE_PARAMETER_LIST", "TYPE_PARAMETER_LIST"),
            "R" to setOf("PARAMETER_LIST", "ARGUMENT_LIST", "R_PARAMETER_LIST"),
            "PERL" to setOf("PARAMETER_LIST", "ARGUMENT_LIST", "PERL5:PARAMETER_LIST"),
            "OBJECTIVE-C" to setOf("PARAMETER_LIST", "ARGUMENT_LIST", "OBJC:PARAMETER_LIST"),
            "HASKELL" to setOf("PARAMETER_LIST", "ARGUMENT_LIST", "HS:PARAMETER_LIST"),
            "F#" to setOf("PARAMETER_LIST", "ARGUMENT_LIST", "FS:PARAMETER_LIST", "FS:ARGUMENT_LIST"),
            "GROOVY" to setOf("PARAMETER_LIST", "ARGUMENT_LIST", "EXPRESSION_LIST"),
            "CLOJURE" to setOf("VECTOR", "LIST", "PARAMETER_LIST"),
            "LUA" to setOf("PARAMETER_LIST", "ARGUMENT_LIST", "FUNCTION_CALL"),
        )
    }

    override fun getLanguageTargetTypes(language: String): Set<String> {
        return languageArgumentTypes[language] ?: emptySet()
    }

    /**
     * There may be different kind of arguments in the same sequence, e.g., in Kotlin named and positional arguments.
     */
    override fun isItem(sourceItem: PsiElement, otherElement: PsiElement): Boolean {
        return checkIsItem(sourceItem, otherElement)
    }

}
