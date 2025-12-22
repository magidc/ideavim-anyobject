package com.magidc.ideavim.anyobject.handlers

import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.common.TextRange
import com.magidc.ideavim.anyobject.handlers.base.TSBasedHandler
import com.magidc.ideavim.anyobject.utils.TSDocument
import com.magidc.ideavim.anyobject.utils.TSModelExtensions.Companion.isEqual
import org.treesitter.TSNode

open class AnyItemHandler : TSBasedHandler() {

    override val targetTypes: Set<String> = setOf("array", "array_initializer", "list", "tuple", "initializer_expression")

    override var acceptNode: (TSNode) -> Boolean = { n -> n.grammarType == "pair" || !n.parent.isNull && n.isNamed && targetTypes.contains(n.parent.grammarType) }

    override fun allowsCountSelection(): Boolean = true

    override fun findInnerBlockRange(node: TSNode, offset: Int, tsDocument: TSDocument): TextRange = tsDocument.toTextRange(node)

    override fun findSelection(editor: VimEditor, inner: Boolean, size: Int): TextRange? {
        if (size == 0) return null
        val tsDocument = getTSDocument(editor)
        val firstNode = tsDocument.findSelectionNode { acceptNode(it) } ?: return null
        val nodes = mutableListOf<TSNode>()
        nodes.add(firstNode)
        @Suppress("unused")
        for (i in 1 until size) {
            tsDocument.findNextNode(nodes.last(), { acceptNode(it) }, loop = false)
                ?.takeIf { it.parent.isEqual(firstNode.parent) }
                ?.let { nodes.add(it) }
                ?: break
        }
        if (nodes.isEmpty()) return null
        if (!inner) {
            if (firstNode.isEqual(firstNode.parent.getNamedChild(0)))
                nodes.last().nextSibling?.takeIf { it.grammarType == "," }?.let { nodes.add(it) }
            else
                firstNode.prevSibling?.takeIf { it.grammarType == "," }?.let { nodes.add(0, it) }
        }
        return tsDocument.toTextRange(nodes.first(), nodes.last())
    }
}

