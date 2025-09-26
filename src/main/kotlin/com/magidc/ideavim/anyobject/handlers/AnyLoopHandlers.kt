package com.magidc.ideavim.anyobject.handlers

import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.magidc.ideavim.anyobject.handlers.base.AbstractPSIBasedHandler
import com.magidc.ideavim.anyobject.handlers.base.BaseHandler
import com.magidc.ideavim.anyobject.handlers.base.HandlerFactory


class AnyLoopHandlers : HandlerFactory {

    override fun getInnerHandler(): BaseHandler {
        return AnyLoopHandler(isInner = true)
    }

    override fun getOuterHandler(size: Int): BaseHandler {
        return AnyLoopHandler(isInner = false)
    }
}

private class AnyLoopHandler(isInner: Boolean) : AbstractPSIBasedHandler(isInner) {
    companion object {
        private val languageLoopTypes = mapOf(
            "JAVA" to setOf("FOR_STATEMENT", "FOREACH_STATEMENT", "WHILE_STATEMENT", "DO_WHILE_STATEMENT", "ENHANCED_FOR_STATEMENT"),
            "KOTLIN" to setOf("FOR", "WHILE", "DO_WHILE", "FOR_EXPRESSION", "WHILE_EXPRESSION"),
            "C#" to setOf("FOR-STATEMENT", "WHILE-STATEMENT", "DO-STATEMENT", "FOREACH-STATEMENT", "CS:FOR-STATEMENT", "CS:WHILE-STATEMENT"),
            "PYTHON" to setOf("FOR_STATEMENT", "WHILE_STATEMENT", "PYFOR", "PYWHILE"),
            "JAVASCRIPT" to setOf("FOR_STATEMENT", "WHILE_STATEMENT", "DO_WHILE_STATEMENT", "FOR_IN_STATEMENT", "FOR_OF_STATEMENT"),
            "ECMASCRIPT 6" to setOf("FOR_STATEMENT", "WHILE_STATEMENT", "DO_WHILE_STATEMENT", "FOR_IN_STATEMENT", "FOR_OF_STATEMENT"),
            "TYPESCRIPT" to setOf("FOR_STATEMENT", "WHILE_STATEMENT", "DO_WHILE_STATEMENT", "FOR_IN_STATEMENT", "FOR_OF_STATEMENT", "JS:FOR_STATEMENT"),
            "DART" to setOf("FOR_STATEMENT", "WHILE_STATEMENT", "DO_STATEMENT", "FOR_IN_STATEMENT"),
            "GO" to setOf("FOR_STATEMENT", "RANGE_STATEMENT", "FOR_CLAUSE", "RANGE_CLAUSE"),
            "PHP" to setOf("FOR_STATEMENT", "WHILE_STATEMENT", "DO_WHILE_STATEMENT", "FOREACH_STATEMENT"),
            "RUBY" to setOf("FOR_STATEMENT", "WHILE_STATEMENT", "UNTIL_STATEMENT", "RUBY:FOR", "RUBY:WHILE", "RUBY:UNTIL"),
            "SCALA" to setOf("FOR_STATEMENT", "WHILE_STATEMENT", "FOR_EXPRESSION", "WHILE_EXPRESSION"),
            "SWIFT" to setOf("FOR_STATEMENT", "WHILE_STATEMENT", "REPEAT_WHILE_STATEMENT", "FOR_IN_STATEMENT"),
            "RUST" to setOf("FOR_EXPR", "WHILE_EXPR", "LOOP_EXPR", "FOR_EXPRESSION", "WHILE_EXPRESSION", "LOOP_EXPRESSION"),
            "R" to setOf("FOR_STATEMENT", "WHILE_STATEMENT", "R_FOR_STATEMENT", "R_WHILE_STATEMENT"),
            "PERL" to setOf("FOR_STATEMENT", "WHILE_STATEMENT", "UNTIL_STATEMENT", "PERL5:FOR", "PERL5:WHILE", "PERL5:UNTIL"),
            "OBJECTIVE-C" to setOf("OBJC:FOR_STATEMENT", "OBJC:WHILE_STATEMENT", "OBJC:DO_WHILE_STATEMENT", "OBJC:FOR_IN_STATEMENT"),
            "HASKELL" to setOf("HS:DO_EXPRESSION", "HS:LIST_COMPREHENSION", "HS:MONAD_COMPREHENSION"),
            "F#" to setOf("FS:FOR_EXPRESSION", "FS:WHILE_EXPRESSION", "FS:FOR_LOOP", "FS:WHILE_LOOP"),
            "GROOVY" to setOf("FOR_STATEMENT", "WHILE_STATEMENT", "FOR_IN_STATEMENT", "EACH_STATEMENT"),
            "CLOJURE" to setOf("FOR", "WHILE", "DOSEQ", "DOTIMES", "LOOP"),
            "LUA" to setOf("FOR_STATEMENT", "WHILE_STATEMENT", "REPEAT_STATEMENT", "FOR_NUMERIC", "FOR_GENERIC"),
            "CPP" to setOf("FOR_STATEMENT", "WHILE_STATEMENT", "DO_WHILE_STATEMENT", "RANGE_FOR_STATEMENT"),
            "C" to setOf("FOR_STATEMENT", "WHILE_STATEMENT", "DO_WHILE_STATEMENT"),
        )
    }

    override fun acceptElement(element: PsiElement, language: String): Boolean {
        val elementTypeName = element.elementType.toString().uppercase()
        val loopTypes = languageLoopTypes[language] ?: emptySet()

        return loopTypes.any { loopType ->
            when {
                loopType.contains("-") -> elementTypeName.contains(loopType) || elementTypeName.endsWith(loopType)
                loopType.contains(":") -> elementTypeName == loopType || elementTypeName.contains(loopType)
                loopType.contains("_") -> elementTypeName.contains(loopType) || elementTypeName.endsWith(loopType) || elementTypeName.startsWith(loopType)
                else -> elementTypeName.contains(loopType) || elementTypeName == loopType
            }
        }
    }
}
