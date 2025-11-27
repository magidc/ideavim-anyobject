package com.magidc.ideavim.anyobject.handlers

import com.intellij.psi.PsiElement


class AnyArgumentHandler : AnyItemHandler() {

    override fun acceptElement(element: PsiElement, language: String, acceptedNormalizedTypes: Set<String>): Boolean {
        if (element.text.isBlank() || isDelimiter(element)) return false
        val parentElementTypeName = element.parent?.toElementTypeName() ?: return false
        if (!parentElementTypeName.endsWith("LIST")) return false
        val elementTypeName = element.toElementTypeName()
        if (language == "C#")
            return parentElementTypeName.endsWith("FUN-CALL-ROLE") || parentElementTypeName.endsWith("PARENT-LIST")
        val normalizedParentElementTypeName = normalizeElementType(parentElementTypeName)
        return (normalizedParentElementTypeName.endsWith("ARGUMENT") && (elementTypeName.contains("ARGUMENT") || elementTypeName.contains("EXPRESSION")))
                || (normalizedParentElementTypeName.endsWith("PARAMETER") && (elementTypeName.contains("PARAMETER")
                || elementTypeName.contains("VARIABLE") || elementTypeName.contains("EXPRESSION")))
                || (normalizedParentElementTypeName.endsWith("EXPRESSION") && elementTypeName.contains("EXPRESSION"))
    }

    private fun normalizeElementType(elementTypeName: String): String {
        return elementTypeName.removeSuffix("LIST")
            .replace("_", "")
            .replace("-", "")
            .replace(" ", "")
    }
}
