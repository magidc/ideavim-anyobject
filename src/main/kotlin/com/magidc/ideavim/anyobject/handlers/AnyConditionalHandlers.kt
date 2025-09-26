package com.magidc.ideavim.anyobject.handlers

import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.magidc.ideavim.anyobject.handlers.base.AbstractPSIBasedHandler
import com.magidc.ideavim.anyobject.handlers.base.BaseHandler
import com.magidc.ideavim.anyobject.handlers.base.HandlerFactory


class AnyConditionalHandlers : HandlerFactory {

    override fun getInnerHandler(): BaseHandler {
        return AnyConditionalHandler(isInner = true)
    }

    override fun getOuterHandler(size: Int): BaseHandler {
        return AnyConditionalHandler(isInner = false)
    }
}

private class AnyConditionalHandler(isInner: Boolean) : AbstractPSIBasedHandler(isInner) {
    companion object {
        private val languageConditionalTypes = mapOf(
            "JAVA" to setOf("IF_STATEMENT", "SWITCH_STATEMENT", "SWITCH_EXPRESSION", "CONDITIONAL_EXPRESSION", "TERNARY_EXPRESSION", "TRY_STATEMENT"),
            "KOTLIN" to setOf("IF", "WHEN", "IF_EXPRESSION", "WHEN_EXPRESSION", "ELVIS_EXPRESSION", "TRY"),
            "C#" to setOf("IF-STATEMENT", "SWITCH-STATEMENT", "CONDITIONAL-EXPRESSION", "CS:IF-STATEMENT", "CS:SWITCH-STATEMENT", "CS:TRY-STATEMENT"),
            "PYTHON" to setOf("IF_STATEMENT", "ELIF_STATEMENT", "MATCH_STATEMENT", "CONDITIONAL_EXPRESSION", "PYIF", "PYMATCH", "TRY_STATEMENT"),
            "JAVASCRIPT" to setOf("IF_STATEMENT", "SWITCH_STATEMENT", "CONDITIONAL_EXPRESSION", "TERNARY_EXPRESSION", "TRY_STATEMENT"),
            "ECMASCRIPT 6" to setOf("IF_STATEMENT", "SWITCH_STATEMENT", "CONDITIONAL_EXPRESSION", "TERNARY_EXPRESSION", "TRY_STATEMENT"),
            "TYPESCRIPT" to setOf("IF_STATEMENT", "SWITCH_STATEMENT", "CONDITIONAL_EXPRESSION", "JS:IF_STATEMENT", "JS:SWITCH_STATEMENT", "TRY_STATEMENT"),
            "DART" to setOf("IF_STATEMENT", "SWITCH_STATEMENT", "CONDITIONAL_EXPRESSION", "IF_ELEMENT", "SWITCH_EXPRESSION", "TRY_STATEMENT"),
            "GO" to setOf("IF_STATEMENT", "SWITCH_STATEMENT", "TYPE_SWITCH", "SELECT_STATEMENT"),
            "PHP" to setOf("IF_STATEMENT", "SWITCH_STATEMENT", "TERNARY_EXPRESSION", "MATCH_EXPRESSION", "TRY_STATEMENT"),
            "RUBY" to setOf("IF_STATEMENT", "CASE_STATEMENT", "UNLESS_STATEMENT", "RUBY:IF", "RUBY:CASE", "RUBY:UNLESS", "BEGIN_STATEMENT"),
            "SCALA" to setOf("IF_STATEMENT", "MATCH_STATEMENT", "IF_EXPRESSION", "MATCH_EXPRESSION", "TRY_STATEMENT"),
            "SWIFT" to setOf("IF_STATEMENT", "SWITCH_STATEMENT", "GUARD_STATEMENT", "DO_CATCH_STATEMENT", "TRY_EXPRESSION"),
            "RUST" to setOf("IF_EXPR", "MATCH_EXPR", "IF_LET_EXPR", "MATCH_ARM", "IF_EXPRESSION", "MATCH_EXPRESSION"),
            "R" to setOf("IF_STATEMENT", "SWITCH_STATEMENT", "R_IF_STATEMENT", "R_SWITCH_STATEMENT", "TRY_STATEMENT"),
            "PERL" to setOf("IF_STATEMENT", "UNLESS_STATEMENT", "GIVEN_STATEMENT", "PERL5:IF", "PERL5:UNLESS", "PERL5:GIVEN", "EVAL_STATEMENT"),
            "OBJECTIVE-C" to setOf("OBJC:IF_STATEMENT", "OBJC:SWITCH_STATEMENT", "OBJC:TRY_STATEMENT", "@TRY_STATEMENT"),
            "HASKELL" to setOf("HS:IF_EXPRESSION", "HS:CASE_EXPRESSION", "HS:GUARD_EXPRESSION"),
            "F#" to setOf("FS:IF_EXPRESSION", "FS:MATCH_EXPRESSION", "FS:CONDITIONAL_EXPRESSION", "FS:TRY_EXPRESSION"),
            "GROOVY" to setOf("IF_STATEMENT", "SWITCH_STATEMENT", "TERNARY_EXPRESSION", "ELVIS_EXPRESSION", "TRY_STATEMENT"),
            "CLOJURE" to setOf("IF", "COND", "CASE", "WHEN", "IF_NOT", "TRY"),
            "LUA" to setOf("IF_STATEMENT", "ELSEIF_STATEMENT", "CONDITIONAL_EXPRESSION"),
            "CPP" to setOf("IF_STATEMENT", "SWITCH_STATEMENT", "CONDITIONAL_EXPRESSION", "TERNARY_EXPRESSION", "TRY_STATEMENT"),
            "C" to setOf("IF_STATEMENT", "SWITCH_STATEMENT", "CONDITIONAL_EXPRESSION", "TERNARY_EXPRESSION"),
        )
    }

    override fun acceptElement(element: PsiElement, language: String): Boolean {
        val elementTypeName = element.elementType.toString().uppercase()
        val conditionalTypes = languageConditionalTypes[language] ?: emptySet()

        return conditionalTypes.any() { conditionalType ->
            when {
                conditionalType.contains("-") -> elementTypeName.contains(conditionalType) || elementTypeName.endsWith(conditionalType)
                conditionalType.contains(":") -> elementTypeName == conditionalType || elementTypeName.contains(conditionalType)
                conditionalType.contains("_") -> elementTypeName.contains(conditionalType) || elementTypeName.endsWith(conditionalType)
                        || elementTypeName.startsWith(conditionalType)

                else -> elementTypeName.contains(conditionalType) || elementTypeName == conditionalType
            }
        }
    }
}
