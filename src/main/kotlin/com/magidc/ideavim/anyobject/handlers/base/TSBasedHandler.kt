package com.magidc.ideavim.anyobject.handlers.base

import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.common.TextRange
import com.magidc.ideavim.anyobject.utils.LRUCache
import com.magidc.ideavim.anyobject.utils.TSDocument
import com.magidc.ideavim.anyobject.utils.TSModelExtensions.Companion.getFirstNamedChildWithGrammar
import org.treesitter.TSNode


abstract class AbstractTSBasedHandler : BaseSelectionHandler, BaseJumpHandler {

    companion object {
        private val documentCache = LRUCache<String, TSDocument>(5) { _, v -> v.editor.document.removeChangeListener(v) }
    }

    protected open val innerBlockTypes: Collection<String> = setOf("block")
    protected abstract val targetTypes: Set<String>

    protected fun getTSDocument(editor: VimEditor): TSDocument = documentCache.getOrPut(editor.getVirtualFile()?.path ?: "") { TSDocument(editor) }

    protected open fun acceptNode(node: TSNode): Boolean = !node.isNull && node.isNamed && targetTypes.contains(node.grammarType)

    override fun findSelection(editor: VimEditor, inner: Boolean, size: Int): TextRange? {
        val tsDocument = getTSDocument(editor)
        val objectNode = tsDocument.findSelectionNode { acceptNode(it) } ?: return null
        if (!inner) return tsDocument.toTextRange(objectNode)
        return findInnerBlockRange(objectNode, editor.getCareOffset(), tsDocument)
    }

    override fun allowsCountSelection(): Boolean = false

    protected open fun findInnerBlockRange(node: TSNode, offset: Int, tsDocument: TSDocument): TextRange? {
        return node.getFirstNamedChildWithGrammar(innerBlockTypes, offset)
            ?.takeIf { it.namedChildCount > 0 }
            ?.let { tsDocument.toTextRange(it.getNamedChild(0), it.getNamedChild(it.namedChildCount - 1)) }
    }

    final override fun findJumpElementStartOffset(editor: VimEditor, forward: Boolean): Int? {
        return getTSDocument(editor).findJumpElementOffset({ acceptNode(it) }, forward)
    }
}