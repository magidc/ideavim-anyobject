package com.magidc.ideavim.anyobject.handlers

import com.intellij.psi.PsiElement
import com.intellij.psi.util.siblings
import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.common.TextRange
import com.magidc.ideavim.anyobject.handlers.base.AbstractPSIBasedHandler


open class AnyItemHandler : AbstractPSIBasedHandler() {
    override fun allowsCountSelection(): Boolean = true

    override fun cleanPrefix(text: String, language: String): String {
        if (language == "XML") return text.replace("XML", "")
        if (language == "YAML") return text.replace("YAML", "")
        if (language == "JSON") return text.replace("JSON", "")
        return super.cleanPrefix(text, language)
    }

    override fun acceptElement(element: PsiElement): Boolean {
        if (element.text.isBlank()) return false
        val parentElementTypeName = element.parent?.let { getElementTypeName(it) } ?: return false
        val elementTypeName = getElementTypeName(element)
        return (parentElementTypeName.contains("ARRAY") && elementTypeName.contains("LITERAL"))
    }

    private fun <T> List<T>.getOrLast(index: Int): T {
        return getOrElse(index) { last() }
    }

    override fun findSelection(editor: VimEditor, isInner: Boolean, size: Int): TextRange? {
        val currentElement = findCurrentElement(editor) ?: return null
        val objectElement = findObjectElement(currentElement) ?: super.getNextElement(currentElement) ?: return null

        if (objectElement.text.isBlank() || size == 0) return null

        var leftOffset: Int = objectElement.textRange.startOffset
        var rightOffset = objectElement.textRange.endOffset

        // Returns a maximum of "size" elements after the current one. If "size" elements are returned, the last one will be out of the target range.
        val elementsAfter = getNextElements(objectElement, size)

        if (isInner) {
            if (elementsAfter.size >= 2)
                return TextRange(leftOffset, elementsAfter[elementsAfter.size - 2].textRange.endOffset)
            return TextRange(leftOffset, rightOffset)
        }

        val elementBefore = getPreviousElement(objectElement, false)

        rightOffset = if (size > 1 && elementsAfter.isNotEmpty()) elementsAfter.getOrLast(size - 2).textRange.endOffset else rightOffset

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
        val siblings = objectElement.parent.children.filter { acceptElement(it) }.toList()
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
        val siblings = objectElement.parent.children.filter { acceptElement(it) }.toList()
        val idx = siblings.indexOf(objectElement)
        if (idx < 0) return null
        return if (siblings.size - 1 == idx) return siblings.first() else siblings[idx + 1]
    }

    private fun getNextElements(element: PsiElement, size: Int): List<PsiElement> {
        if (size <= 0) return emptyList()
        return element.siblings(withSelf = false).filter { acceptElement(it) }.take(size).toList()
    }
}
