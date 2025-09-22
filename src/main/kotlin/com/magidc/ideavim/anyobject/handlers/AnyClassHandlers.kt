package com.magidc.ideavim.anyobject.handlers

import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.maddyhome.idea.vim.api.VimEditor
import com.magidc.ideavim.anyobject.handlers.base.AbstractPSIBasedHandler
import com.magidc.ideavim.anyobject.handlers.base.BaseHandler
import com.magidc.ideavim.anyobject.handlers.base.HandlerFactory


class AnyClassHandlers : HandlerFactory {
    override fun getInnerHandler(): BaseHandler {
        return AnyClassHandler(isInner = true)
    }

    override fun getOuterHandler(): BaseHandler {
        return AnyClassHandler(isInner = false)
    }

    private class AnyClassHandler(isInner: Boolean) : AbstractPSIBasedHandler(isInner) {
        companion object {
            private val languageClassTypes = mapOf(
                "JAVA" to setOf("CLASS", "INTERFACE", "ENUM", "ANNOTATION_TYPE", "RECORD"),
                "KOTLIN" to setOf("CLASS", "INTERFACE", "OBJECT_DECLARATION", "ENUM_ENTRY", "ANNOTATION_CLASS"),
                "C#" to setOf(
                    "CLASS-DECLARATION",
                    "INTERFACE-DECLARATION",
                    "STRUCT-DECLARATION",
                    "ENUM-DECLARATION",
                    "RECORD-DECLARATION",
                    "CS:CLASS-DECLARATION",
                    "CS:INTERFACE-DECLARATION",
                    "CS:STRUCT-DECLARATION"
                ),
                "PYTHON" to setOf("PYCLASS", "CLASS_DECLARATION"),
                "JAVASCRIPT" to setOf("JS:CLASS", "CLASS_EXPRESSION", "ES6_CLASS", "CLASS_DECLARATION"),
                "ECMASCRIPT 6" to setOf("JS:CLASS", "CLASS_EXPRESSION", "ES6_CLASS", "CLASS_DECLARATION"),
                "TYPESCRIPT" to setOf("JS:TYPESCRIPT_CLASS", "JS:TYPESCRIPT_INTERFACE", "JS:TYPESCRIPT_ENUM", "JS:TYPESCRIPT_NAMESPACE", "JS:TYPESCRIPT_MODULE"),
                "DART" to setOf("CLASS_DECLARATION", "MIXIN_DECLARATION", "ENUM_DECLARATION", "EXTENSION_DECLARATION", "ABSTRACT_CLASS_DECLARATION"),
                "GO" to setOf("TYPE_DECLARATION", "STRUCT_TYPE", "INTERFACE_TYPE", "TYPE_SPEC"),
                "RUST" to setOf("STRUCT", "ENUM", "TRAIT", "IMPL", "UNION", "STRUCT_ITEM", "ENUM_ITEM", "TRAIT_ITEM"),
                "PHP" to setOf("CLASS_DECLARATION", "INTERFACE_DECLARATION", "TRAIT_DECLARATION", "ENUM_DECLARATION", "ABSTRACT_CLASS_DECLARATION"),
                "RUBY" to setOf("RUBY:CLASS", "RUBY:MODULE", "RUBY:SINGLETON_CLASS"),
                "SCALA" to setOf("CLASS DEFINITION", "OBJECT DEFINITION", "TRAIT DEFINITION", "CASE CLASS DEFINITION", "ABSTRACT CLASS DEFINITION", "SEALED CLASS DEFINITION"),
                "SWIFT" to setOf("CLASS_DECLARATION", "STRUCT_DECLARATION", "PROTOCOL_DECLARATION", "ENUM_DECLARATION", "EXTENSION_DECLARATION", "ACTOR_DECLARATION"),
                "R" to setOf("R_CLASS_DEFINITION", "R_SETCLASS"),
                "PERL" to setOf("PERL5:PACKAGE", "PERL5:NAMESPACE"),
                "OBJECTIVE-C" to setOf("OBJC:CLASS_DECLARATION", "OBJC:PROTOCOL_DECLARATION", "OBJC:CATEGORY_DECLARATION", "OBJC:INTERFACE_DECLARATION"),
                "HASKELL" to setOf("HS:DATA_DECLARATION", "HS:NEWTYPE_DECLARATION", "HS:TYPE_DECLARATION", "HS:CLASS_DECLARATION", "HS:INSTANCE_DECLARATION"),
                "F#" to setOf("FS:TYPE_DEFINITION", "FS:INTERFACE_DEFINITION", "FS:MODULE_DEFINITION", "FS:RECORD_DEFINITION", "FS:UNION_DEFINITION"),
                "GROOVY" to setOf("CLASS", "INTERFACE", "ENUM", "TRAIT_DECLARATION"),
                "CLOJURE" to setOf("DEFTYPE", "DEFRECORD", "DEFPROTOCOL", "REIFY"),
                "LUA" to setOf("TABLE", "METATABLE", "CLASS_DECLARATION"),
                "CPP" to setOf("CLASS_DECLARATION", "STRUCT_DECLARATION", "UNION_DECLARATION", "NAMESPACE_DECLARATION"),
                "C" to setOf("STRUCT_DECLARATION", "UNION_DECLARATION", "ENUM_DECLARATION"),
            )

            private val languageClassStatementBlockTypes = mapOf(
                "KOTLIN" to "CLASS_BODY",
                "PYTHON" to "PYSTATEMENTLIST",
                "DART" to "CLASS_BODY",
                "PHP" to "CLASS_STATEMENT_LIST",
                "RUBY" to "BODY_STATEMENT",
                "SCALA" to "TEMPLATE_BODY",
                "SWIFT" to "CODE_BLOCK",
                "R" to "R_BLOCK_EXPRESSION",
                "PERL" to "PERL5:BLOCK",
                "OBJECTIVE-C" to "COMPOUND_STATEMENT",
                "HASKELL" to "HS:WHERE_CLAUSES",
                "F#" to "FS:TYPE_BODY",
                "CLOJURE" to "VECTOR",
                "LUA" to "CHUNK",
                "CPP" to "COMPOUND_STATEMENT",
                "C" to "COMPOUND_STATEMENT",
            )
        }

        override fun acceptElement(element: PsiElement, language: String): Boolean {
            val elementTypeName = element.elementType.toString().uppercase()
            val classTypes = languageClassTypes[language] ?: emptySet()

            return classTypes.any { classType ->
                when {
                    classType.contains("-") -> elementTypeName.endsWith(classType)
                    classType.contains(":") -> elementTypeName == classType
                    classType.contains("_") -> elementTypeName.endsWith(classType) || elementTypeName.startsWith(classType)
                    classType.contains(" ") -> elementTypeName.endsWith(classType)
                    else -> elementTypeName == classType || elementTypeName.endsWith(classType)
                }
            }
        }

        override fun findInnerCodeBlock(element: PsiElement, editor: VimEditor): PsiElement? {
            val innerCodeBlockElementTypeName = languageClassStatementBlockTypes[element.language.id.uppercase()] ?: return element

            val elementQueue = ArrayDeque<PsiElement>()
            elementQueue.add(element)
            while (elementQueue.isNotEmpty()) {
                val currentElement = elementQueue.removeFirst()
                val currentElementTypeName = currentElement.elementType.toString().uppercase()
                if (currentElementTypeName == innerCodeBlockElementTypeName)
                    return currentElement
                elementQueue.addAll(currentElement.children)
            }
            return null
        }
    }
}