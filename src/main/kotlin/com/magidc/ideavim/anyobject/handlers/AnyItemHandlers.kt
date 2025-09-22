package com.magidc.ideavim.anyobject.handlers

import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.psi.util.siblings
import com.maddyhome.idea.vim.api.VimEditor
import com.magidc.ideavim.anyobject.handlers.base.AbstractPSIBasedHandler
import com.magidc.ideavim.anyobject.handlers.base.BaseHandler
import com.magidc.ideavim.anyobject.handlers.base.HandlerFactory
import com.magidc.ideavim.anyobject.model.Selection


class AnyItemHandlers : HandlerFactory {
    override fun getInnerHandler(): BaseHandler {
        return AnyItemHandler(isInner = true)
    }

    override fun getOuterHandler(): BaseHandler {
        return AnyItemHandler(isInner = false)
    }
}

open class AnyItemHandler(isInner: Boolean) : AbstractPSIBasedHandler(isInner) {
    companion object {
        private val languageTargetTypes = mapOf(
            "JAVA" to setOf("LIST", "ARRAY_INITIALIZER_EXPRESSION"),
            "KOTLIN" to setOf("LIST", "ARRAY", "COLLECTION_LITERAL_EXPRESSION"),
            "PYTHON" to setOf("LIST", "DICT_LITERAL_EXPRESSION", "ARRAY", "SET_LITERAL_EXPRESSION", "LIST_LITERAL_EXPRESSION"),
            "JAVASCRIPT" to setOf("ARRAY", "LIST", "OBJECT_LITERAL", "ARRAY_LITERAL_EXPRESSION"),
            "ECMASCRIPT 6" to setOf("ARRAY", "LIST", "OBJECT_LITERAL", "ARRAY_LITERAL_EXPRESSION"),
            "TYPESCRIPT" to setOf("ARRAY", "LIST", "OBJECT_LITERAL", "ARRAY_LITERAL_EXPRESSION", "TUPLE_TYPE"),
            "C#" to setOf("ARRAY_INITIALIZER_EXPRESSION", "LIST", "COLLECTION_INITIALIZER"),
            "DART" to setOf("LIST", "ARRAY", "LIST_LITERAL", "SET_LITERAL", "MAP_LITERAL"),
            "GO" to setOf("ARRAY", "SLICE", "COMPOSITE_LIT"),
            "PHP" to setOf("ARRAY", "ARRAY_CREATION_EXPRESSION"),
            "RUBY" to setOf("ARRAY", "LIST", "HASH_LITERAL"),
            "SCALA" to setOf("LIST", "ARRAY", "SEQUENCE_LITERAL", "SET_LITERAL"),
            "SWIFT" to setOf("ARRAY", "DICTIONARY", "ARRAY_LITERAL", "DICTIONARY_LITERAL"),
            "RUST" to setOf("ARRAY", "VEC", "ARRAY_EXPRESSION", "VEC_MACRO"),
            "R" to setOf("LIST", "VECTOR", "LIST_EXPRESSION"),
            "PERL" to setOf("ARRAY", "LIST", "ARRAY_REF"),
            "OBJECTIVE-C" to setOf("ARRAY", "DICTIONARY", "NS_ARRAY_LITERAL"),
            "HASKELL" to setOf("LIST", "ARRAY", "LIST_EXPRESSION"),
            "F#" to setOf("LIST", "ARRAY", "LIST_EXPRESSION", "ARRAY_EXPRESSION"),
            "GROOVY" to setOf("LIST", "ARRAY", "LIST_EXPRESSION"),
            "CLOJURE" to setOf("VECTOR", "LIST", "MAP", "SET"),
            "LUA" to setOf("TABLE", "ARRAY"),

            // Data format languages
            "JSON" to setOf("ARRAY", "OBJECT", "JSON_ARRAY", "JSON_OBJECT"),
            "XML" to setOf("XML_TAG", "XML_ELEMENT", "XML_ATTRIBUTE_LIST"),
            "YAML" to setOf("SEQUENCE", "MAPPING", "YAML_SEQUENCE", "YAML_MAPPING", "YAML_ARRAY", "YAML_HASH"),
        )
    }

    open fun getLanguageTargetTypes(language: String): Set<String> {
        return languageTargetTypes[language] ?: emptySet()
    }

    override fun acceptElement(element: PsiElement, language: String): Boolean {
        val containerElementTypeName = element.parent.elementType.toString().uppercase()
        return getLanguageTargetTypes(language).any { containerElementTypeName.endsWith(it) }
    }

    override fun getSelection(element: PsiElement, editor: VimEditor): Selection? {
        if (element.text.isBlank()) return null

        var leftOffset: Int = element.textRange.startOffset
        var rightOffset: Int = element.textRange.endOffset

        if (isInner) return Selection(leftOffset, rightOffset)

        val elementBefore = element.siblings(forward = false, withSelf = false).filter { isItem(element, it) }.firstOrNull()
        val elementAfter = element.siblings(withSelf = false).filter { isItem(element, it) }.firstOrNull()
        if (null == elementBefore) {
            if (null != elementAfter)
                rightOffset = elementAfter.textRange.startOffset
        } else
            leftOffset = elementBefore.textRange.endOffset
        return Selection(leftOffset, rightOffset)
    }

    open fun isItem(sourceItem: PsiElement, otherElement: PsiElement): Boolean {
        val language = sourceItem.language.id.uppercase()

        return when (language) {
            "JSON" -> isJsonItem(sourceItem, otherElement)
            "XML" -> isXmlItem(sourceItem, otherElement)
            "YAML" -> isYamlItem(sourceItem, otherElement)
            else -> sourceItem.elementType == otherElement.elementType
        }
    }

    private fun isJsonItem(sourceItem: PsiElement, otherElement: PsiElement): Boolean {
        val sourceType = sourceItem.elementType.toString().uppercase()
        val otherType = otherElement.elementType.toString().uppercase()

        return sourceType == otherType ||
                (sourceType.contains("VALUE") && otherType.contains("VALUE")) ||
                (sourceType.contains("PROPERTY") && otherType.contains("PROPERTY"))
    }

    private fun isXmlItem(sourceItem: PsiElement, otherElement: PsiElement): Boolean {
        val sourceType = sourceItem.elementType.toString().uppercase()
        val otherType = otherElement.elementType.toString().uppercase()

        return sourceType == otherType ||
                (sourceType.contains("XML_TAG") && otherType.contains("XML_TAG")) ||
                (sourceType.contains("XML_ATTRIBUTE") && otherType.contains("XML_ATTRIBUTE"))
    }

    private fun isYamlItem(sourceItem: PsiElement, otherElement: PsiElement): Boolean {
        val sourceType = sourceItem.elementType.toString().uppercase()
        val otherType = otherElement.elementType.toString().uppercase()

        return sourceType == otherType ||
                (sourceType.contains("SEQUENCE_ITEM") && otherType.contains("SEQUENCE_ITEM")) ||
                (sourceType.contains("KEY_VALUE") && otherType.contains("KEY_VALUE")) ||
                (sourceType.contains("MAPPING") && otherType.contains("MAPPING"))
    }
}
