package com.magidc.ideavim.anyobject.handlers

import com.intellij.psi.PsiElement
import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.common.TextRange


class AnyArgumentHandler : AnyItemHandler() {

    override fun findSelection(editor: VimEditor, isInner: Boolean, size: Int): TextRange? {
        val range = super.findSelection(editor, isInner, size) ?: return null

        // For PHP named arguments, expand selection to include parameter name and colon
        val currentElement = findCurrentElement(editor) ?: return range
        val language = getLanguage(currentElement)
        if (language != "PHP") return range

        val objectElement = findObjectElement(currentElement) ?: return range

        // Check if this element is part of a named argument by looking for preceding IDENTIFIER and COLON
        var prevSibling = objectElement.prevSibling
        var colonElement: PsiElement? = null
        var identifierElement: PsiElement? = null

        // Find COLON (skip whitespace)
        while (prevSibling != null && prevSibling.text.isBlank()) {
            prevSibling = prevSibling.prevSibling
        }
        if (prevSibling?.toElementTypeName() == "COLON") {
            colonElement = prevSibling
            prevSibling = prevSibling.prevSibling

            // Find IDENTIFIER (skip whitespace)
            while (prevSibling != null && prevSibling.text.isBlank()) {
                prevSibling = prevSibling.prevSibling
            }
            if (prevSibling?.toElementTypeName() == "IDENTIFIER") {
                identifierElement = prevSibling
            }
        }

        // If we found a named argument pattern, expand the range
        if (identifierElement != null && colonElement != null) {
            val newStart = identifierElement.textRange.startOffset
            return TextRange(newStart, range.endOffset)
        }

        return range
    }

    override fun acceptElement(element: PsiElement, language: String, acceptedNormalizedTypes: Set<String>): Boolean {
        if (element.text.isBlank() || isDelimiter(element)) return false
        val parentElementTypeName = element.parent?.toElementTypeName() ?: return false
        if (!parentElementTypeName.endsWith("LIST")) return false
        val elementTypeName = element.toElementTypeName()

        if (language == "C#")
            return parentElementTypeName.endsWith("PARENT-LIST") && (
                    elementTypeName.contains("PARAMETER-DECLARATION")
                            || elementTypeName.contains("LITERAL")
                            || elementTypeName.contains("CALL")
                    )

        // PHP uses "PARAMETER LIST" (with space) for both function definitions and calls
        // Accept any non-delimiter child of PARAMETER LIST, except IDENTIFIER and COLON (which are part of named arguments)
        if (language == "PHP" && parentElementTypeName.contains("PARAMETER") && parentElementTypeName.endsWith("LIST"))
            return !elementTypeName.contains("IDENTIFIER") && !elementTypeName.contains("COLON")

        return (parentElementTypeName.endsWith("ARGUMENT_LIST") && (elementTypeName.contains("ARGUMENT") || elementTypeName.contains("EXPRESSION")))
                || (parentElementTypeName.endsWith("PARAMETER_LIST") && elementTypeName.contains("PARAMETER"))
                || (parentElementTypeName.endsWith("EXPRESSION_LIST") && elementTypeName.contains("EXPRESSION"))
    }

}
