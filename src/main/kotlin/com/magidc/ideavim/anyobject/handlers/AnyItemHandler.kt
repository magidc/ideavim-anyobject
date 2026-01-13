package com.magidc.ideavim.anyobject.handlers

import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.common.TextRange
import com.magidc.ideavim.anyobject.handlers.base.TSBasedHandler
import com.magidc.ideavim.anyobject.utils.TSDocument
import com.magidc.ideavim.anyobject.utils.TSModelExtensions.Companion.isEqual
import org.treesitter.TSNode

/**
 * Handler for targeting any item within a collection (array, list, tuple, dictionary, etc.).
 */
open class AnyItemHandler : TSBasedHandler() {

    open val parentTargetTypes: Set<String> = setOf(
        "array", "array_initializer", "list", "tuple", "initializer_expression", "composite_literal",
        "literal_value", "dictionary", "set", "element_list", "sequence", "collection", "object", "array_literal",
        "tuple_expression", "token_tree", "array_creation_expression", "initializer_list", "dictionary_literal"
    )
    override val targetTypes: Set<String> = setOf("pair", "flow_node")

    override fun acceptNode(node: TSNode, document: TSDocument): Boolean {
        return super.acceptNode(node, document)
                || !node.parent.isNull && node.isNamed && parentTargetTypes.contains(node.parent.grammarType)
    }

    override fun allowsCountSelection(): Boolean = true

    override fun findInnerBlockRange(currentNode: TSNode, objectNode: TSNode, offset: Int, tsDocument: TSDocument): TextRange = tsDocument.toTextRange(objectNode)

    override fun findSelection(editor: VimEditor, inner: Boolean, size: Int): TextRange? {
        if (size == 0) return null
        val tsDocument = getTSDocument(editor)
        val firstNode = tsDocument.findSelectionNode({ acceptNode(it, tsDocument) }) ?: return null
        val nodes = generateSequence(firstNode) { tsDocument.findNextNode(it, { n -> acceptNode(n, tsDocument) }, loop = false) }
            .filter { it.parent.isEqual(firstNode.parent) }
            .take(size).toMutableList()

        if (nodes.isEmpty()) return null
        if (!inner) {
            if (firstNode.isEqual(firstNode.parent.getNamedChild(0)))
                nodes.last().nextSibling?.takeIf { !it.isNull && it.grammarType == "," }?.let { nodes.add(it) }
            else
                firstNode.prevSibling?.takeIf { !it.isNull && it.grammarType == "," }?.let { nodes.add(0, it) }
        }
        return tsDocument.toTextRange(nodes.first(), nodes.last())
    }
}

