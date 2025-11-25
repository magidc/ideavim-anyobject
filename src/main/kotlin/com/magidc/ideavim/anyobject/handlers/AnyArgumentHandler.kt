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

        return (parentElementTypeName.endsWith("ARGUMENT_LIST") && (elementTypeName.contains("ARGUMENT") || elementTypeName.contains("EXPRESSION")))
                || (parentElementTypeName.endsWith("PARAMETER_LIST") && elementTypeName.contains("PARAMETER"))
                || (parentElementTypeName.endsWith("EXPRESSION_LIST") && elementTypeName.contains("EXPRESSION"))
    }

}
