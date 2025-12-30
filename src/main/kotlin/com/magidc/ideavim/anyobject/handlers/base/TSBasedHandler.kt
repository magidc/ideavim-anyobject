package com.magidc.ideavim.anyobject.handlers.base

import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.common.TextRange
import com.magidc.ideavim.anyobject.utils.LRUCache
import com.magidc.ideavim.anyobject.utils.TSDocument
import com.magidc.ideavim.anyobject.utils.TSModelExtensions.Companion.getFirstNamedChildWithGrammar
import com.magidc.ideavim.anyobject.utils.TSModelExtensions.Companion.lastNamedLeafOrSelf
import com.magidc.ideavim.anyobject.utils.TSModelExtensions.Companion.nextNamedLeaf
import com.magidc.ideavim.anyobject.utils.TSModelExtensions.Companion.prevLeaf
import org.treesitter.TSNode


abstract class TSBasedHandler : BaseSelectionHandler, BaseJumpHandler {

    companion object {
        val documentCache = LRUCache<String, TSDocument>(5) { _, v -> v.editor.document.removeChangeListener(v) }
    }

    protected open val innerBlockTypes: Collection<String> = setOf("block")
    abstract val targetTypes: Set<String>

    protected fun getTSDocument(editor: VimEditor): TSDocument = documentCache.getOrPut(editor.getVirtualFile()?.path ?: "") { TSDocument(editor) }

    protected open val acceptNode: (TSNode) -> Boolean = { n -> !n.isNull && n.isNamed && targetTypes.contains(n.grammarType) }

    override fun findSelection(editor: VimEditor, inner: Boolean, size: Int): TextRange? {
        val tsDocument = getTSDocument(editor)
        val objectNode = tsDocument.findSelectionNode { acceptNode(it) } ?: return null
        if (!inner) return tsDocument.toTextRange(objectNode)
        return findInnerBlockRange(objectNode, editor.getCareOffset(), tsDocument)
    }

    override fun allowsCountSelection(): Boolean = false

    open fun findInnerBlockRange(node: TSNode, offset: Int, tsDocument: TSDocument): TextRange? {
        return node.getFirstNamedChildWithGrammar(innerBlockTypes, offset)
            ?.takeIf { it.namedChildCount > 0 }
            ?.let {
                val fromNode = it.nextNamedLeaf() ?: it
                val toNode = (it.getChild(it.childCount - 1).let { x -> if (x.grammarType == "}" || x.grammarType == "end") x.prevLeaf() else x })?.takeIf { x -> !x.isNull }
                    ?: it.lastNamedLeafOrSelf()
                tsDocument.toTextRange(fromNode, toNode)
            }
    }

    final override fun findJumpElementStartOffset(editor: VimEditor, forward: Boolean): Int? {
        return getTSDocument(editor).findJumpElementOffset({ acceptNode(it) }, forward)
    }

}