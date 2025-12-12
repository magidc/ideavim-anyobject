package com.magidc.ideavim.anyobject.handlers

import com.maddyhome.idea.vim.common.TextRange
import com.magidc.ideavim.anyobject.handlers.base.AbstractTSBasedHandler
import com.magidc.ideavim.anyobject.utils.TSDocument
import org.treesitter.TSNode

class AnyFieldHandler : AbstractTSBasedHandler() {
    override val targetTypes = setOf(
        "property_declaration", "field_declaration", "field_definition", "local_declaration_statement", "local_variable_declaration", "declaration"
    )

    override fun findInnerBlockRange(node: TSNode, offset: Int, tsDocument: TSDocument): TextRange {
        return node.getFirstChildWithGrammar(setOf("="))?.nextChild()
            ?.takeIf { !it.isNull }
            ?.let {
                if (it.namedChildCount > 0) {
                    var lastLeaf: TSNode = node.lastLeafOrSelf()
                    while (lastLeaf.grammarType == ";")
                        lastLeaf = lastLeaf.prevLeaf() ?: break
                    tsDocument.toTextRange(it, lastLeaf)
                } else tsDocument.toTextRange(it)
            }
            ?: tsDocument.toTextRange(node)
    }
}