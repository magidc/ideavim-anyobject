package com.magidc.ideavim.anyobject.handlers.base

import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.util.elementType
import com.maddyhome.idea.vim.api.VimEditor
import com.magidc.ideavim.anyobject.model.Selection
import java.nio.file.Path

abstract class AbstractPSIBasedHandler(isInner: Boolean, val size: Int = 1) : BaseHandler(isInner) {

    companion object {
        private val languageCodeBlockTypes = mapOf(
            "JAVA" to setOf("CODE_BLOCK"),
            "C#" to setOf("CS:BLOCK-LIST"),
            "DART" to setOf("FUNCTION_BODY"),
            "KOTLIN" to setOf("BLOCK"),
            "RUST" to setOf("BLOCK"),
            "PHP" to setOf("GROUP STATEMENT"),
            "SCALA" to setOf("BLOCK OF EXPRESSIONS"),
            "TYPESCRIPT" to setOf("BLOCK_STATEMENT"),
            "JAVASCRIPT" to setOf("BLOCK_STATEMENT"),
            "ECMASCRIPT 6" to setOf("BLOCK_STATEMENT"),
            "PERL" to setOf("PERL5: BLOCK"),
            "PYTHON" to setOf("PYSTATEMENTLIST"),
            "RUBY" to setOf("BODY STATEMENT"),
            "R" to setOf("R_BLOCK_EXPRESSION")
        )

        private val languageUsesDelimiters = mapOf(
            "JAVA" to true,
            "KOTLIN" to true,
            "C#" to true,
            "PYTHON" to false,
            "JAVASCRIPT" to true,
            "ECMASCRIPT 6" to true,
            "TYPESCRIPT" to true,
            "DART" to true,
            "GO" to true,
            "RUST" to true,
            "PHP" to true,
            "RUBY" to false,
            "SCALA" to true,
            "R" to true,
            "PERL" to true,
            "F#" to false,
            "GROOVY" to true,
            "CLOJURE" to true,
            "LUA" to false,
            "CPP" to true,
            "C" to true,
        )
    }

    protected open fun findInnerCodeBlock(element: PsiElement, editor: VimEditor): PsiElement? {
        val language = element.language.id.uppercase()
        val codeBlockTypes = languageCodeBlockTypes[language] ?: emptySet()
        val caretOffset = editor.currentCaret().offset

        val elementQueue = ArrayDeque<PsiElement>()
        elementQueue.add(element)

        while (elementQueue.isNotEmpty()) {
            val currentElement = elementQueue.removeFirst()
            if (!currentElement.textRange.contains(caretOffset))
                continue
            val elementTypeName = currentElement.elementType.toString().uppercase()
            if (codeBlockTypes.any { blockType ->
                    when {
                        blockType.contains(":") -> elementTypeName == blockType || elementTypeName.contains(blockType)
                        blockType.contains(" ") -> elementTypeName == blockType
                        else -> elementTypeName == blockType || elementTypeName.contains(blockType)
                    }
                })
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
        return getSelection(itemElement, editor)
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

    protected open fun getSelection(element: PsiElement, editor: VimEditor): Selection? {
        if (isInner) {
            val innerBlock = findInnerCodeBlock(element, editor) ?: element
            if (languageUsesDelimiters.getOrDefault(element.language.id.uppercase(), false)) {
                val openBrace = innerBlock.children.firstOrNull { it.elementType.toString().uppercase() == "LBRACE" }
                if (null != openBrace) {
                    val closeBrace = innerBlock.children.firstOrNull { it.elementType.toString().uppercase() == "RBRACE" }
                    if (null != closeBrace)
                        return Selection(openBrace.textRange.endOffset, closeBrace.textRange.startOffset)
                }
                return Selection(innerBlock.textRange.startOffset + 1, innerBlock.textRange.endOffset - 1)
            }
            return Selection(innerBlock.textRange.startOffset, innerBlock.textRange.endOffset)
        }
        return Selection(element.textRange.startOffset, element.textRange.endOffset)
    }
}