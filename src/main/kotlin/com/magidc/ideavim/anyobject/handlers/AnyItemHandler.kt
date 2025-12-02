package com.magidc.ideavim.anyobject.handlers

import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.common.TextRange
import com.magidc.ideavim.anyobject.handlers.base.AbstractTSBasedHandler
import org.treesitter.TSNode


open class AnyItemHandler : AbstractTSBasedHandler() {
    protected open val parentTypes = setOf("argument", "simple_parameter")

    override fun acceptNode(node: TSNode): Boolean = !node.parent.isNull && parentTypes.contains(node.parent.grammarType)

    override fun allowsCountSelection(): Boolean = true

    override fun findInnerBlock(node: TSNode?, offset: Int): TSNode? = node

    override fun findSelection(editor: VimEditor, inner: Boolean, size: Int): TextRange? {
        val tsDocument = getTSDocument(editor)
        if (size == 0) return null
        val firstNode = tsDocument.findSelectionNode { acceptNode(it) } ?: return null
        val nodes = mutableListOf<TSNode>()
        nodes.add(firstNode)
        for (i in 1 until size) {
            tsDocument.findNext(nodes.last(), { acceptNode(it) })
                ?.takeIf { it.parent.isEqual(firstNode.parent) }
                ?.let { nodes.add(it) }
                ?: break
        }
        if (nodes.isEmpty()) return null
        return tsDocument.toTextRange(nodes.first(),nodes.last())
    }
}

