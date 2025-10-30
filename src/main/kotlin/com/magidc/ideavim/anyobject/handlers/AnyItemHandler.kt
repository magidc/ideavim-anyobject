package com.magidc.ideavim.anyobject.handlers

import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.psi.util.siblings
import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.common.TextRange
import com.magidc.ideavim.anyobject.handlers.base.AbstractPSIBasedHandler
import com.magidc.ideavim.anyobject.handlers.base.BaseJumpHandler


open class AnyItemHandler : AbstractPSIBasedHandler(), BaseJumpHandler {
    companion object {
        private val commonTargetTypes = setOf("LIST", "ARRAY", "COLLECTION", "MAP", "SET", "SEQUENCE", "TUPLE")
        private val languageTargetTypes = mapOf(
            "JAVA" to setOf("ARRAYINITIALIZER"),
            "JAVASCRIPT" to setOf("OBJECT"),
            "TYPESCRIPT" to setOf("TUPLETYPE"),
            "C#" to setOf("ARRAYINITIALIZER", "COLLECTIONINITIALIZER"),
            "GO" to setOf("SLICE", "COMPOSITELIT"),
            "PHP" to setOf("ARRAYCREATION"),
            "RUBY" to setOf("HASH"),
            "SWIFT" to setOf("DICTIONARY"),
            "RUST" to setOf("VEC", "VECMACRO"),
            "R" to setOf("VECTOR"),
            "PERL" to setOf("ARRAYREF"),
            "OBJECTIVE-C" to setOf("DICTIONARY", "NSARRAY"),
            "CLOJURE" to setOf("VECTOR"),
            "LUA" to setOf("TABLE"),
            // Data format languages
            "JSON" to setOf("OBJECT"),
            "XML" to setOf("TAG", "ELEMENT", "ATTRIBUTELIST"),
            "YAML" to setOf("MAPPING", "MAPPING", "HASH"),
        )
    }

    override fun getLanguageSpecificTypes(): Map<String, Set<String>> = languageTargetTypes

    override fun cleanPrefix(text: String, language: String): String {
        if (language == "XML") return text.replace("XML_", "")
        if (language == "YAML") return text.replace("YAML_", "")
        if (language == "JSON") return text.replace("JSON_", "")
        return super.cleanPrefix(text, language)
    }

    override fun getSuffixes(): Set<String> = super.getSuffixes() + setOf("LITERAL")

    override fun getCommonTypes(): Set<String> = commonTargetTypes

    override fun acceptElement(element: PsiElement, language: String, acceptedNormalizedTypes: Set<String>): Boolean {
        if (element.text.isBlank()) return false
        val elementTypeName = element.elementType.toString().lowercase()
        if (elementTypeName.contains("comma")) return false
        if (elementTypeName.contains("lpar")) return false
        if (elementTypeName.contains("rpar")) return false

        val containerElementTypeName = element.parent.elementType.toString().uppercase()
        return acceptedNormalizedTypes.contains(normalizeElementType(containerElementTypeName, language))
    }

    override fun getSelection(element: PsiElement, editor: VimEditor, isInner: Boolean, size: Int): TextRange? {
        if (element.text.isBlank()) return null

        var leftOffset: Int = element.textRange.startOffset
        var rightOffset = element.textRange.endOffset

        if (isInner) return TextRange(leftOffset, element.textRange.endOffset)

        val elementBefore = getPreviousElement(element, false)
        val elementsAfter = getNextElements(element, size)

        rightOffset = if (elementsAfter.isNotEmpty() && elementsAfter.size < size) elementsAfter.last().textRange.endOffset else rightOffset

        if (null == elementBefore) {
            val elementAfterSelection = if (elementsAfter.size == size) elementsAfter.last() else null
            if (null != elementAfterSelection)
                rightOffset = elementAfterSelection.textRange.startOffset
        } else
            leftOffset = elementBefore.textRange.endOffset

        return TextRange(leftOffset, rightOffset)
    }

    private fun getPreviousElement(element: PsiElement, loop: Boolean): PsiElement? {
        // If the current element is not an item, fallback to the default handler behavior to find the previous one in the document from the current position
        val objectElement = findObjectElement(element) ?: return super.getPreviousElement(element)
        val siblings = objectElement.parent.children.filter { isItem(objectElement, it) }.toList()
        val idx = siblings.indexOf(objectElement)
        if (idx < 0) return null
        if (siblings.isEmpty()) return null
        if (siblings.first() == objectElement)
            return if (loop) siblings.last() else null
        return siblings[idx - 1]
    }

    /**
     * For jumps, items iterated in loop
     */
    override fun getPreviousElement(element: PsiElement): PsiElement? {
        return getPreviousElement(element, true)
    }

    /**
     * For jumps, items iterated in loop
     */
    override fun getNextElement(element: PsiElement): PsiElement? {
        // If the current element is not an item, fallback to the default handler behavior to find the first one in the document from the current position
        val objectElement = findObjectElement(element) ?: return super.getNextElement(element)
        val siblings = objectElement.parent.children.filter { isItem(objectElement, it) }.toList()
        val idx = siblings.indexOf(objectElement)
        if (idx < 0) return null
        if (siblings.isEmpty()) return null
        if (siblings.last() == objectElement) return siblings.first()
        return siblings[idx + 1]
    }

    private fun getNextElements(element: PsiElement, size: Int): List<PsiElement> {
        return element.siblings(withSelf = false).filter { isItem(element, it) }.take(size).toList()
    }

    /**
     * Given an element (sourceItem) that is an item targeted by this handler, check if the given element (otherElement) is also an item.
     */
    private fun isItem(sourceItem: PsiElement, otherElement: PsiElement): Boolean {
        val language = getLanguage(sourceItem)
        return when (language) {
            "JSON" -> isJsonItem(sourceItem, otherElement)
            "XML" -> isXmlItem(sourceItem, otherElement)
            "YAML" -> isYamlItem(sourceItem, otherElement)
            else -> {
                val elementTypeName = otherElement.elementType.toString().lowercase()
                return sourceItem.elementType == otherElement.elementType || (
                        otherElement.text.isNotBlank()
                                // Sometimes argument separators do not appear as children of the main element parent (Pycharm)
                                && otherElement.parent.children.any { it.elementType == otherElement.elementType }
                                && !elementTypeName.contains("comma")
                                && !elementTypeName.contains("lpar")
                                && !elementTypeName.contains("rpar")
                        )
            }
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
