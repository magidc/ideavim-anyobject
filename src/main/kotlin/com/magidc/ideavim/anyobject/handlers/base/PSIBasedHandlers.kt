package com.magidc.ideavim.anyobject.handlers.base

import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.util.childLeafs
import com.intellij.psi.util.elementType
import com.intellij.psi.util.startOffset
import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.common.TextRange
import com.magidc.ideavim.anyobject.utils.LRUCache
import java.nio.file.Path

/**
 * Base class for all handlers that operate on Intellij PSI DOM
 */
abstract class AbstractPSIBasedHandler : BaseSelectionHandler, BaseJumpHandler {

    companion object {
        fun PsiElement.toElementTypeName(): String = this.elementType.toString().uppercase().trim()
        private val cleanDelimitersRegex = "[_,-]".toRegex()
        private val languagesWithoutDelimiters = setOf("PYTHON", "RUBY", "F#", "LUA")
        val languageCodeBlockTypes = mapOf(
            "JAVA" to setOf("CODE_BLOCK"),
            "KOTLIN" to setOf("BLOCK"),
            "C#" to setOf("CS:BLOCK-LIST", "CS:STATEMENTS-LIST"),
            "DART" to setOf("FUNCTION_BODY"),
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
    }

    private val acceptedNormalizedTypesCache = LRUCache<String, Set<String>>(4)
    protected open val commonTypes: Set<String> = emptySet()
    protected open val languageSpecificTypes: Map<String, Set<String>> = emptyMap()
    protected open fun getCommonSuffixes() = setOf("STATEMENT", "EXPRESSION")
    private val cleanSuffixesRegex = "(${getCommonSuffixes().joinToString("|")})\$".toRegex()


    /**
     * Utility method to print all element type names from the given element to its parents in the PSI DOM hierarchy
     */
    protected fun printElementHierarchyType(element: PsiElement, textLimit: Int = 50, startElement: Boolean = true) {
        if (startElement)
            println("--------------------")
        println("Type: ${element.toElementTypeName()}")
        println("Text: ${element.text.take(textLimit)}")
        if (element.parent != null)
            printElementHierarchyType(element.parent, textLimit, false)
        if (startElement)
            println("--------------------")
    }

    /**
     * Finds the inner code block for the given element (for inner selections)
     */
    protected open fun findInnerCodeBlock(currentElement: PsiElement, objectElement: PsiElement, editor: VimEditor): PsiElement? {
        val codeBlockTypes = getCodeBlockTypes(objectElement)
        val caretOffset = editor.getCareOffset()

        val elementQueue = ArrayDeque<PsiElement>()
        elementQueue.add(objectElement)

        while (elementQueue.isNotEmpty()) {
            val currentElement = elementQueue.removeFirst()
            if (currentElement.textRange.endOffset < caretOffset)
                continue
            val elementTypeName = currentElement.toElementTypeName()
            if (codeBlockTypes.any { blockType ->
                    when {
                        blockType.contains(" ") -> elementTypeName == blockType
                        else -> elementTypeName.contains(blockType)
                    }
                }) return currentElement
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
        val objectElement = findObjectElement(currentElement) ?: getNextElement(currentElement, false) ?: return null
        return getSelection(currentElement, objectElement, editor, isInner)
    }

    private fun getCurrentPSIFile(editor: VimEditor): PsiFile? {
        val vimVirtualFile = editor.getVirtualFile() ?: return null
        val projectManager = ProjectManager.getInstance()
        if (null == projectManager || projectManager.openProjects.isEmpty())
            return null
        val project = projectManager.openProjects[0]
        val virtualFile = VirtualFileManager.getInstance().findFileByNioPath(Path.of(vimVirtualFile.path)) ?: return null
        return PsiManager.getInstance(project).findFile(virtualFile)
    }

    protected fun findCurrentElement(editor: VimEditor): PsiElement? {
        return getCurrentPSIFile(editor)?.findElementAt(editor.getCareOffset())
    }

    protected fun findObjectElement(currentElement: PsiElement): PsiElement? {
        val language = getLanguage(currentElement)
        val types = getAcceptedNormalizedTypes(language)
        var objectElement = currentElement
        while (!objectElement.toElementTypeName().endsWith("FILE") && objectElement.parent != null) {
            if (acceptElement(objectElement, language, types)) return objectElement
            objectElement = objectElement.parent
        }
        return null
    }

    private fun normalizeElementType(elementTypeName: String, language: String): String {
        var normalizedText = elementTypeName
        if (normalizedText.contains(":"))
            normalizedText = normalizedText.substringAfter(":")
        normalizedText = cleanPrefix(normalizedText, language)
        return normalizedText.replace(cleanDelimitersRegex, "").replace(cleanSuffixesRegex, "").trim()
    }

    private fun normalizeElementType(element: PsiElement, language: String): String {
        return normalizeElementType(element.toElementTypeName(), language)
    }

    private fun getAcceptedNormalizedTypes(language: String): Set<String> {
        return acceptedNormalizedTypesCache.computeIfAbsent(language) { commonTypes + (languageSpecificTypes[language] ?: emptySet()) }
    }


    /**
     * Evaluates whether the given element matches the type the handler is looking for.
     */
    protected open fun acceptElement(element: PsiElement, language: String, acceptedNormalizedTypes: Set<String>): Boolean {
        return acceptedNormalizedTypes.contains(normalizeElementType(element, language))
    }

    private fun PsiElement.toTextRange(): TextRange = TextRange(this.textRange.startOffset, this.textRange.endOffset)

    private fun getSelection(currentElement: PsiElement, objectElement: PsiElement, editor: VimEditor, isInner: Boolean): TextRange {
        if (isInner) {
            val innerBlock = findInnerCodeBlock(currentElement, objectElement, editor) ?: objectElement
            if (!languagesWithoutDelimiters.contains(getLanguage(objectElement))) {
                val openBrace = innerBlock.childLeafs().firstOrNull { it.text == "{" }
                if (null != openBrace) {
                    val closeBrace = innerBlock.childLeafs(forward = false).firstOrNull { it.text == "}" }
                    if (null != closeBrace)
                        return TextRange(openBrace.textRange.endOffset, closeBrace.textRange.startOffset)
                }
            }
            return innerBlock.toTextRange()
        }
        return objectElement.toTextRange()
    }


    private fun getPreviousElement(element: PsiElement): PsiElement? {
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

    protected fun getNextElement(element: PsiElement, restart: Boolean = true): PsiElement? {
        val language = getLanguage(element)
        val acceptedNormalizedTypes = getAcceptedNormalizedTypes(language)
        val file = element.containingFile
        var next = findNextElement(element)
        while (null != next) {
            if (next.containingFile != file) {
                if (!restart) return null
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

    protected fun getLanguage(element: PsiElement): String = element.language.id.uppercase()

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