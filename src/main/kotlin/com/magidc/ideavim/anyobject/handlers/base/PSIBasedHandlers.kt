package com.magidc.ideavim.anyobject.handlers.base

import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.util.childLeafs
import com.intellij.psi.util.elementType
import com.intellij.psi.util.startOffset
import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.common.TextRange
import java.nio.file.Path

/**
 * Base class for all handlers that operate on Intellij PSI DOM
 */
abstract class AbstractPSIBasedHandler : BaseSelectionHandler, BaseJumpHandler {

    companion object {
        private val cleanDelimitersRegex = "[_,-]".toRegex()
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

    protected open val commonTypes: Set<String> = emptySet()
    protected open fun getCommonSuffixes() = setOf("STATEMENT", "EXPRESSION")
    protected open val languageSpecificTypes: Map<String, Set<String>> = emptyMap()
    private val cleanSuffixesRegex = "(${getCommonSuffixes().joinToString("|")})\$".toRegex()

    /**
     * Finds the inner code block for the given element (for inner selections)
     */
    protected open fun findInnerCodeBlock(element: PsiElement, editor: VimEditor): PsiElement? {
        val codeBlockTypes = getCodeBlockTypes(element)
        val caretOffset = editor.currentCaret().offset

        val elementQueue = ArrayDeque<PsiElement>()
        elementQueue.add(element)

        while (elementQueue.isNotEmpty()) {
            val currentElement = elementQueue.removeFirst()
            if (!currentElement.textRange.contains(caretOffset))
                continue
            val elementTypeName = getElementTypeName(currentElement)
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

    /**
     * Getting the element types that represent code blocks for the given language.
     */
    protected open fun getCodeBlockTypes(element: PsiElement): Set<String> = languageCodeBlockTypes[getLanguage(element)] ?: emptySet()


    override fun findSelection(editor: VimEditor, isInner: Boolean, size: Int): TextRange? {
        val currentElement = findCurrentElement(editor) ?: return null
        val objectElement = findObjectElement(currentElement) ?: return null
        return getSelection(objectElement, editor, isInner, size)
    }

    private fun findCurrentElement(editor: VimEditor): PsiElement? {
        val projectManager = ProjectManager.getInstance()
        if (null == projectManager || projectManager.openProjects.isEmpty()) return null
        val project = projectManager.openProjects[0]
        val vimVirtualFile = editor.getVirtualFile() ?: return null
        val virtualFile = VirtualFileManager.getInstance().findFileByNioPath(Path.of(vimVirtualFile.path)) ?: return null

        val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
        return psiFile?.findElementAt(editor.currentCaret().offset)
    }

    protected fun findObjectElement(currentElement: PsiElement): PsiElement? {
        val language = getLanguage(currentElement)
        val types = getAcceptedNormalizedTypes(language)
        var objectElement = currentElement
        while (!getElementTypeName(objectElement).endsWith("FILE") && objectElement.parent != null) {
            if (acceptElement(objectElement, language, types)) return objectElement
            objectElement = objectElement.parent
        }
        return null
    }

    protected fun normalizeElementType(text: String, language: String): String {
        var normalizedText = text.uppercase().trim()
        if (normalizedText.contains(":"))
            normalizedText = normalizedText.substringAfter(":")
        normalizedText = cleanPrefix(normalizedText, language)
        return normalizedText.replace(cleanDelimitersRegex, "").replace(cleanSuffixesRegex, "").trim()
    }

    private fun getAcceptedNormalizedTypes(language: String): Set<String> =
        commonTypes + (languageSpecificTypes[language] ?: emptySet())

    /**
     * Evaluates whether the given element matches the type the handler is looking for.
     */
    protected open fun acceptElement(element: PsiElement, language: String, acceptedNormalizedTypes: Set<String>): Boolean {
        val elementTypeName = getElementTypeName(element)
        return acceptedNormalizedTypes.contains(normalizeElementType(elementTypeName, language))
    }

    protected open fun getSelection(element: PsiElement, editor: VimEditor, isInner: Boolean, size: Int): TextRange? {
        if (isInner) {
            val innerBlock = findInnerCodeBlock(element, editor) ?: element
            if (languageUsesDelimiters.getOrDefault(getLanguage(element), false)) {
                val openBrace = innerBlock.parent.childLeafs().firstOrNull { it.text == "{" || getElementTypeName(it) == "LBRACE" }
                if (null != openBrace) {
                    val closeBrace = innerBlock.parent.childLeafs().lastOrNull { it.text == "}" || getElementTypeName(it) == "RBRACE" }
                    if (null != closeBrace)
                        return TextRange(openBrace.textRange.endOffset, closeBrace.textRange.startOffset)
                }
                return TextRange(innerBlock.textRange.startOffset, innerBlock.textRange.endOffset)
            }
            return TextRange(innerBlock.textRange.startOffset, innerBlock.textRange.endOffset)
        }
        return TextRange(element.textRange.startOffset, element.textRange.endOffset)
    }

    protected fun getElementTypeName(element: PsiElement): String = element.elementType.toString().uppercase()

    open fun getPreviousElement(element: PsiElement): PsiElement? {
        val language = getLanguage(element)
        val acceptedNormalizedTypes = getAcceptedNormalizedTypes(language)
        val file = element.containingFile
        var previous = findPreviousElement(element)
        while (null != previous) {
            if (previous.containingFile != file) {
                previous = getNestedLastChild(file)
                continue
            }
            if (previous == element) return null
            // It is necessary to validate that we are not repeatedly returning the same effective position unless the same element is found (when there is only one)
            if (acceptElement(previous, language, acceptedNormalizedTypes) && previous.textRange.startOffset != element.startOffset) return previous
            previous = findPreviousElement(previous)
        }
        return null
    }

    private fun findPreviousElement(element: PsiElement): PsiElement? {
        val prevSibling = element.prevSibling
        if (prevSibling != null)
            return getNestedLastChild(prevSibling)
        if (element.parent != null) return element.parent
        return null
    }

    protected open fun cleanPrefix(text: String, language: String): String {
        if (language == "R") return text.substringAfter("R")
        if (language == "PYTHON") return text.substringAfter("PY")
        return text
    }

    open fun getNextElement(element: PsiElement): PsiElement? {
        val language = getLanguage(element)
        val acceptedNormalizedTypes = getAcceptedNormalizedTypes(language)
        val file = element.containingFile
        var next = findNextElement(element)
        while (null != next) {
            if (next.containingFile != file) {
                next = file
                continue
            }
            if (next == element) return null
            // It is necessary to validate that we are not repeatedly returning the same effective position unless the same element is found (when there is only one)
            if (acceptElement(next, language, acceptedNormalizedTypes) && next.textRange.startOffset != element.startOffset) return next
            next = findNextElement(next)
        }
        return null
    }

    protected fun getLanguage(currentElement: PsiElement): String = currentElement.language.id.uppercase()

    private fun findNextElement(element: PsiElement): PsiElement? {
        if (element.firstChild != null)
            return element.firstChild
        if (element.nextSibling != null)
            return element.nextSibling
        var ancestor = element.parent
        while (ancestor != null) {
            if (ancestor.nextSibling != null)
                return ancestor.nextSibling
            ancestor = ancestor.parent
        }
        return null
    }


    private fun getNestedLastChild(element: PsiElement): PsiElement {
        var lastChild = element
        while (lastChild.lastChild != null) {
            lastChild = lastChild.lastChild
        }
        return lastChild
    }

    override fun findJumpElementStartOffset(editor: VimEditor, next: Boolean): Int? {
        val currentElement = findCurrentElement(editor) ?: return null
        return (if (next) getNextElement(currentElement) else getPreviousElement(currentElement))?.textRange?.startOffset
    }
}