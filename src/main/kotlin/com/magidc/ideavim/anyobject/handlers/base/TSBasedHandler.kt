package com.magidc.ideavim.anyobject.handlers.base

import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.common.TextRange
import com.magidc.ideavim.anyobject.utils.LRUCache
import com.magidc.ideavim.anyobject.utils.TSDocument
import com.magidc.ideavim.anyobject.utils.TSLanguageUtils
import com.magidc.ideavim.anyobject.utils.TSModelExtensions.Companion.getFirstNamedChildWithGrammar
import com.magidc.ideavim.anyobject.utils.TSModelExtensions.Companion.lastLeafOrSelf
import com.magidc.ideavim.anyobject.utils.TSModelExtensions.Companion.toText
import org.treesitter.TSNode
import kotlin.reflect.KClass


abstract class TSBasedHandler : BaseSelectionHandler, BaseJumpHandler {

    companion object {
        val documentCache = LRUCache<String, TSDocument>(5)
        private val assigmentSymbols = setOf("=", "=>")
    }

    protected open val innerBlockTypes: Set<String> = setOf("block")
    abstract val targetTypes: Set<String>

    protected open val languageTargetTypes: Map<KClass<*>, Set<String>> = emptyMap()

    protected fun getTSDocument(editor: VimEditor): TSDocument = documentCache.getOrPut(editor.getVirtualFile()?.path ?: "") { TSDocument(editor) }

    protected open fun acceptNode(node: TSNode, document: TSDocument): Boolean {
        return !node.isNull && node.isNamed && (
                languageTargetTypes.getOrDefault(document.parser.language::class, emptySet()).contains(node.grammarType)
                        || targetTypes.contains(node.grammarType)
                )
    }

    override fun findSelection(editor: VimEditor, inner: Boolean, size: Int): TextRange? {
        val tsDocument = getTSDocument(editor)
        val currentNode = tsDocument.findCurrentNode() ?: return null
        val objectNode = tsDocument.findSelectionNode({ acceptNode(it, tsDocument) }, currentNode) ?: return null
        if (!inner) return tsDocument.toTextRange(objectNode)
        return findInnerBlockRange(currentNode, objectNode, editor.getCaretOffset(), tsDocument)
    }

    override fun allowsCountSelection(): Boolean = false

    open fun findInnerBlockRange(currentNode: TSNode, objectNode: TSNode, offset: Int, tsDocument: TSDocument): TextRange? {
        return objectNode.getFirstNamedChildWithGrammar(innerBlockTypes, offset)
            ?.takeIf { it.namedChildCount > 0 }
            ?.let { getCodeBlock(it, tsDocument) }
    }

    protected fun getCodeBlock(node: TSNode, tsDocument: TSDocument): TextRange? {
        if (node.childCount == 0) return null
        if (node.childCount >= 2 && assigmentSymbols.contains(node.getChild(0).grammarType))
            return tsDocument.toTextRange(node.getChild(1), node)
        if (tsDocument.languageInfo.blockType == TSLanguageUtils.TSBlockType.BRACES)
            return getBracesCodeBlock(node, tsDocument) ?: tsDocument.toTextRange(node)
        if (tsDocument.languageInfo.blockType == TSLanguageUtils.TSBlockType.END)
            return getBracesCodeBlock(node, tsDocument) ?: getEndCodeBlock(node, tsDocument) ?: tsDocument.toTextRange(node)
        return tsDocument.toTextRange(node)
    }

    protected fun getBracesCodeBlock(node: TSNode, tsDocument: TSDocument): TextRange? {
        val nodeText = node.toText(tsDocument.editor, Int.MAX_VALUE)
        val openBraceOffset = nodeText.indexOf('{').takeIf { it > -1 } ?: return null
        val closeBraceOffset = nodeText.lastIndexOf('}').takeIf { it > -1 } ?: return null
        val textOffset = tsDocument.toCharOffset(node.startByte)
        return TextRange(textOffset + openBraceOffset + 1, textOffset + closeBraceOffset)
    }

    private fun getEndCodeBlock(node: TSNode, tsDocument: TSDocument): TextRange? {
        if (node.lastLeafOrSelf().grammarType != "end") return null
        val nodeText = node.toText(tsDocument.editor, Int.MAX_VALUE)
        val firstLineEnd = nodeText.indexOf('\n').takeIf { it > -1 } ?: return null
        return TextRange(
            tsDocument.toCharOffset(node.startByte) + firstLineEnd,
            tsDocument.toCharOffset(node.endByte) - 3
        )
    }

    final override fun findJumpElement(editor: VimEditor, forward: Boolean): TextRange? {
        val tsDocument = getTSDocument(editor)
        return tsDocument.findJumpElement({ acceptNode(it, tsDocument) }, forward)
    }
}