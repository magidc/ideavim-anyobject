package com.magidc.ideavim.anyobject.handlers

import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.magidc.ideavim.anyobject.handlers.base.AbstractPSIBasedHandler
import com.magidc.ideavim.anyobject.handlers.base.BaseHandler
import com.magidc.ideavim.anyobject.handlers.base.HandlerFactory


class AnyFieldHandlers : HandlerFactory {
    override fun getInnerHandler(): BaseHandler {
        return AnyFieldHandler(isInner = true)
    }

    override fun getOuterHandler(): BaseHandler {
        return AnyFieldHandler(isInner = false)
    }

    class AnyFieldHandler(isInner: Boolean) : AbstractPSIBasedHandler(isInner) {
        companion object {
            private val languageVariableTypes = mapOf(
                "JAVA" to setOf("FIELD", "LOCAL_VARIABLE", "PARAMETER", "ENUM_CONSTANT", "RESOURCE_VARIABLE", "EXCEPTION_PARAMETER", "PATTERN_VARIABLE"),
                "KOTLIN" to setOf("PROPERTY", "LOCAL_VARIABLE", "VALUE_PARAMETER", "OBJECT_DECLARATION", "CLASS_PARAMETER", "LOOP_PARAMETER", "DESTRUCTURING_DECLARATION"),
                "C#" to setOf("CS:FIELD-DECLARATION", "CS:LOCAL-VARIABLE", "CS:PARAMETER", "CS:PROPERTY-DECLARATION", "CS:CONSTANT-DECLARATION", "CS:EVENT-FIELD-DECLARATION"),
                "PYTHON" to setOf("PY:TARGET_EXPRESSION", "PY:PARAMETER", "PY:NAMED_PARAMETER", "PY:TUPLE_PARAMETER", "PY:SINGLE_STAR_PARAMETER", "PY:DOUBLE_STAR_PARAMETER"),
                "JAVASCRIPT" to setOf("JS:VAR_STATEMENT", "JS:LET_STATEMENT", "JS:CONST_STATEMENT", "JS:PARAMETER", "JS:DESTRUCTURING_PARAMETER", "JS:REST_PARAMETER"),
                "TYPESCRIPT" to setOf("JS:TYPESCRIPT_VARIABLE", "JS:TYPESCRIPT_PARAMETER", "JS:TYPESCRIPT_PROPERTY", "JS:TYPESCRIPT_FIELD", "JS:TYPESCRIPT_ACCESSOR"),
                "DART" to setOf("VARIABLE_DECLARATION", "FIELD_DECLARATION", "FORMAL_PARAMETER", "DEFAULT_FORMAL_PARAMETER", "FIELD_FORMAL_PARAMETER"),
                "GO" to setOf("VAR_DECLARATION", "SHORT_VAR_DECLARATION", "FIELD_DECLARATION", "PARAMETER_DECLARATION", "RECEIVER"),
                "RUST" to setOf("LET_DECL", "STATIC_ITEM", "CONST_ITEM", "FIELD_DECL", "VALUE_PARAMETER", "SELF_PARAMETER"),
                "PHP" to setOf("VARIABLE", "FIELD", "PARAMETER", "PROPERTY", "CLASS_CONSTANT", "GLOBAL_VARIABLE"),
                "RUBY" to setOf("RUBY:LOCAL_VARIABLE", "RUBY:INSTANCE_VARIABLE", "RUBY:CLASS_VARIABLE", "RUBY:GLOBAL_VARIABLE", "RUBY:CONSTANT", "RUBY:PARAMETER"),
                "SCALA" to setOf("VALUE DEFINITION", "VARIABLE DEFINITION", "PARAMETER", "CLASS PARAMETER", "PATTERN DEFINITION"),
                "R" to setOf("R_ASSIGNMENT_STATEMENT", "R_PARAMETER", "R_VARIABLE"),
                "PERL" to setOf("PERL5:VARIABLE", "PERL5:SCALAR_VARIABLE", "PERL5:ARRAY_VARIABLE", "PERL5:HASH_VARIABLE", "PERL5:SUB_DECLARATION"),
                "HASKELL" to setOf("HS:VAR_DECLARATION", "HS:PATTERN_BINDING", "HS:FUNCTION_BINDING"),
                "F#" to setOf("FS:BINDING", "FS:PARAMETER", "FS:FIELD", "FS:PROPERTY"),
                "GROOVY" to setOf("FIELD", "LOCAL_VARIABLE", "PARAMETER", "PROPERTY"),
                "CLOJURE" to setOf("DEF", "DEFN", "LET_BINDING", "PARAMETER"),
                "LUA" to setOf("LOCAL_VARIABLE", "GLOBAL_VARIABLE", "PARAMETER", "FIELD"),
                "C" to setOf("VARIABLE_DECLARATION", "FIELD_DECLARATION", "PARAMETER_DECLARATION", "STATIC_VARIABLE", "EXTERN_VARIABLE"),
            )
        }

        override fun acceptElement(element: PsiElement, language: String): Boolean {
            return languageVariableTypes[language]?.contains(element.elementType.toString().uppercase()) ?: false
        }
    }
}