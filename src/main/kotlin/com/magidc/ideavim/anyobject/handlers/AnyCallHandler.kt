package com.magidc.ideavim.anyobject.handlers

import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.common.TextRange
import com.magidc.ideavim.anyobject.handlers.base.TSBasedHandler
import com.magidc.ideavim.anyobject.utils.TSDocument
import com.magidc.ideavim.anyobject.utils.TSModelExtensions.Companion.getFirstNamedChildWithGrammar
import com.magidc.ideavim.anyobject.utils.TSModelExtensions.Companion.toText
import org.treesitter.TSNode

/**
 * Handler for targeting any function call expression
 */
class AnyCallHandler : TSBasedHandler() {

    override val innerBlockTypes = AnyArgumentHandler.parentTargetTypes + setOf("generator_expression", "block", "annotated_lambda")

    override val targetTypes = setOf(
        "call", "call_expression", "method_invocation", "member_call_expression", "function_call_expression", "scoped_call_expression", "invocation_expression",
        "nullsafe_member_call_expression", "expression_statement"
    )

    override fun findSelection(editor: VimEditor, inner: Boolean, size: Int): TextRange? {
        val tsDocument = getTSDocument(editor)
        val currentNode = tsDocument.findCurrentNode() ?: return null
        val objectNode = tsDocument.findSelectionNode({ acceptNode(it, tsDocument) }, currentNode) ?: return null
        if (!inner) {
            val range = tsDocument.toTextRange(objectNode)
            val text = objectNode.toText(tsDocument.editor)
            if (text.endsWith(";"))
                return TextRange(range.startOffset, range.endOffset - 1)
            return range
        }
        return findInnerBlockRange(currentNode, objectNode, tsDocument.getCaretByteOffset(), tsDocument)
    }

    override fun getCodeBlock(node: TSNode, tsDocument: TSDocument): TextRange {
        val text = node.toText(tsDocument.editor)
        val range = tsDocument.toTextRange(node)
        if (
            (text.startsWith("{") && text.endsWith("}"))
            || (text.startsWith("(") && text.endsWith(")"))
        )
            return TextRange(range.startOffset + 1, range.endOffset - 1)
        return range
    }

    override fun findInnerBlockRange(currentNode: TSNode, objectNode: TSNode, byteOffset: Int, tsDocument: TSDocument): TextRange? {
        return objectNode.getFirstNamedChildWithGrammar(innerBlockTypes, byteOffset)
            ?.let { getCodeBlock(it, tsDocument) }
    }
}
