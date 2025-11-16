package com.magidc.ideavim.anyobject.utils

import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType

class DevUtils {
    companion object {
        /**
         * Utility method to print all element type names from the given element to its parents in the PSI DOM hierarchy
         */
        fun printElementHierarchyType(element: PsiElement, textLimit: Int = 50, startElement: Boolean = true) {
            if (startElement)
                println("--------------------")
            println("Type: ${element.elementType.toString()}")
            println("Text: ${element.text.take(textLimit)}")
            if (element.parent != null)
                printElementHierarchyType(element.parent, textLimit, false)
            if (startElement)
                println("--------------------")
        }
    }
}