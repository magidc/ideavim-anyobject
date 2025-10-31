package com.magidc.ideavim.anyobject.handlers

import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.maddyhome.idea.vim.api.VimEditor
import com.magidc.ideavim.anyobject.handlers.base.AbstractPSIBasedHandler


class AnyClassHandler : AbstractPSIBasedHandler() {
    companion object {
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

    override fun getCommonSuffixes(): Set<String> = super.getCommonSuffixes() + setOf("DECLARATION", "DEFINITION")

    override val commonTypes: Set<String> = setOf("CLASS", "INTERFACE", "ENUM")

    override val languageSpecificTypes: Map<String, Set<String>> = mapOf(
        "JAVA" to setOf("ANNOTATIONTYPE"),
        "KOTLIN" to setOf("OBJECT", "ENUMENTRY", "ANNOTATIONCLASS"),
        "C#" to setOf("STRUCT", "RECORD"),
        "JAVASCRIPT" to setOf("ES6"),
        "ECMASCRIPT 6" to setOf("ES6"),
        "TYPESCRIPT" to setOf("NAMESPACE", "MODULE"),
        "DART" to setOf("MIXIN", "EXTENSION", "ABSTRACTCLASS"),
        "GO" to setOf("TYPE", "STRUCTTYPE", "INTERFACETYPE", "TYPESPEC"),
        "RUST" to setOf("STRUCT", "TRAIT", "IMPL", "UNION", "STRUCTITEM", "ENUMITEM", "TRAITITEM"),
        "PHP" to setOf("TRAIT", "ABSTRACTCLASS"),
        "RUBY" to setOf("MODULE", "SINGLETONCLASS"),
        "SCALA" to setOf("OBJECT", "TRAIT", "CASECLASS", "ABSTRACTCLASS", "SEALEDCLASS"),
        "SWIFT" to setOf("STRUCT", "PROTOCOL", "EXTENSION", "ACTOR"),
        "R" to setOf("SETCLASS"),
        "PERL" to setOf("PACKAGE", "NAMESPACE"),
        "OBJECTIVE-C" to setOf("PROTOCOL", "CATEGORY"),
        "HASKELL" to setOf("DATA", "NEWTYPE", "TYPE", "INSTANCE"),
        "F#" to setOf("TYPE", "MODULE", "RECORD", "UNION"),
        "GROOVY" to setOf("TRAIT"),
        "CLOJURE" to setOf("DEFTYPE", "DEFRECORD", "DEFPROTOCOL", "REIFY"),
        "LUA" to setOf("TABLE", "METATABLE"),
        "CPP" to setOf("STRUCT", "UNION", "NAMESPACE"),
        "C" to setOf("STRUCT", "UNION"),
    )

    override fun cleanPrefix(text: String, language: String): String {
        if (language == "TYPESCRIPT") return text.substringAfter("TYPESCRIPT")
        return super.cleanPrefix(text, language)
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