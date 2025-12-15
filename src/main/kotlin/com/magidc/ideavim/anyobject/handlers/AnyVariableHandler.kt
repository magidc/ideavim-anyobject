package com.magidc.ideavim.anyobject.handlers

import com.maddyhome.idea.vim.common.TextRange
import com.magidc.ideavim.anyobject.handlers.base.TSBasedHandler
import com.magidc.ideavim.anyobject.utils.TSDocument
import com.magidc.ideavim.anyobject.utils.TSModelExtensions.Companion.getFirstChildWithGrammar
import com.magidc.ideavim.anyobject.utils.TSModelExtensions.Companion.lastLeafOrSelf
import com.magidc.ideavim.anyobject.utils.TSModelExtensions.Companion.nextLeaf
import com.magidc.ideavim.anyobject.utils.TSModelExtensions.Companion.prevLeaf
import org.treesitter.TSNode

class AnyVariableHandler : TSBasedHandler() {
    override val targetTypes = setOf(
        "assignment", "assignment_expression", "property_declaration", "field_declaration", "field_definition", "local_declaration_statement",
        "local_variable_declaration", "declaration"
    )

    override fun findInnerBlockRange(node: TSNode, offset: Int, tsDocument: TSDocument): TextRange? {
        return node.getFirstChildWithGrammar(setOf("="))?.nextLeaf()
            ?.takeIf { !it.isNull }
            ?.let {
                if (it.namedChildCount > 0) {
                    var lastLeaf: TSNode = node.lastLeafOrSelf()
                    if (lastLeaf.grammarType != ";") tsDocument.toTextRange(it, node)
                    else {
                        while (lastLeaf.grammarType == ";")
                            lastLeaf = lastLeaf.prevLeaf() ?: break
                        tsDocument.toTextRange(it, lastLeaf)
                    }
                } else tsDocument.toTextRange(it, node)
            }
    }
}