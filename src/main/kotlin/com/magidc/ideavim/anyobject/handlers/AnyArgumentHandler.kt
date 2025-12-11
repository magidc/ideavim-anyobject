package com.magidc.ideavim.anyobject.handlers

import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.common.TextRange
import com.magidc.ideavim.anyobject.handlers.base.AbstractTSBasedHandler
import org.treesitter.TSNode

open class AnyArgumentHandler : AbstractTSBasedHandler() {
    override val targetTypes = setOf(
        "arguments", "argument_list", "value_arguments", "formal_parameters", "formal_parameters_list",
        "parameters", "parameter_list", "function_parameters", "primary_constructor", "function_value_parameters",
        "lambda_parameters", "lambda_parameter_list"
    )

    override fun acceptNode(node: TSNode): Boolean = !node.parent.isNull && node.isNamed && targetTypes.contains(node.parent.grammarType)

    override fun allowsCountSelection(): Boolean = true

    override fun findInnerBlock(node: TSNode?, offset: Int): TSNode? = node

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
