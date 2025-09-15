package com.magidc.ideavim.anyobject.handlers.base

import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.util.elementType
import com.maddyhome.idea.vim.api.VimEditor
import com.magidc.ideavim.anyobject.model.Selection
import java.nio.file.Path

abstract class AbstractPSIBasedHandler(isInner: Boolean) : BaseHandler(isInner) {

    protected open fun findInnerCodeBlock(element: PsiElement): PsiElement? {
        val elementQueue = ArrayDeque<PsiElement>()
        elementQueue.add(element)
        while (elementQueue.isNotEmpty()) {
            val currentElement = elementQueue.removeFirst()
            val elementTypeName = currentElement.elementType.toString().uppercase()
            if (elementTypeName == "CODE_BLOCK" // Java
                || elementTypeName == "CS:BLOCK-LIST" // C#
                || elementTypeName == "FUNCTION_BODY" // Dart
                || elementTypeName == "BLOCK" // Kotlin, Rust
                || elementTypeName == "GROUP STATEMENT" // PHP
                || elementTypeName == "BLOCK OF EXPRESSIONS" // Scala
                || elementTypeName == "BLOCK_STATEMENT" // Typescript, Javascript
                || elementTypeName == "PERL5: BLOCK" // Perl
                || elementTypeName == "PYSTATEMENTLIST" // Python
                || elementTypeName == "BODY STATEMENT" // Ruby
                || elementTypeName == "R_BLOCK_EXPRESSION" // R
            )
                return currentElement
            elementQueue.addAll(currentElement.children)
        }
        return null
    }

    override fun findSelection(editor: VimEditor): Selection? {
        val projectManager = ProjectManager.getInstance()
        if (null == projectManager || projectManager.openProjects.isEmpty()) return null
        val project = projectManager.openProjects[0]
        val vimVirtualFile = editor.getVirtualFile() ?: return null
        val virtualFile = VirtualFileManager.getInstance().findFileByNioPath(Path.of(vimVirtualFile.path)) ?: return null

        val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
        val element = psiFile?.findElementAt(editor.currentCaret().offset) ?: return null
        val itemElement = findItemPSIFile(element) ?: return null
        return getSelection(itemElement)
    }

    private fun findItemPSIFile(psiElement: PsiElement): PsiElement? {
        val language = psiElement.language.id.uppercase()
        var currentElement = psiElement
        while (currentElement.parent != null) {
            if (acceptElement(currentElement, language)) return currentElement
            currentElement = currentElement.parent
        }
        return null
    }

    protected abstract fun acceptElement(element: PsiElement, language: String): Boolean

    protected open fun getSelection(element: PsiElement): Selection? {
        if (isInner) {
            val classBody = findInnerCodeBlock(element) ?: element
            val openBrace = classBody.children.firstOrNull { it.elementType.toString().uppercase() == "LBRACE" }
            if (null != openBrace) {
                val closeBrace = classBody.children.firstOrNull { it.elementType.toString().uppercase() == "RBRACE" }
                if (null != closeBrace)
                    return Selection(openBrace.textRange.endOffset, closeBrace.textRange.startOffset)
            }
            return Selection(classBody.textRange.startOffset, classBody.textRange.endOffset)
        }
        return Selection(element.textRange.startOffset, element.textRange.endOffset)
    }
}