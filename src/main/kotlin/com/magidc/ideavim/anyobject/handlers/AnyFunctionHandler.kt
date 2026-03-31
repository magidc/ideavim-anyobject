package com.magidc.ideavim.anyobject.handlers

import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.common.TextRange
import com.magidc.ideavim.anyobject.handlers.base.TSBasedHandler
import com.magidc.ideavim.anyobject.utils.TSLanguageUtils.Companion.DART
import com.magidc.ideavim.anyobject.utils.TSModelExtensions.Companion.getFirstNamedChildWithGrammar
import com.magidc.ideavim.anyobject.utils.TSModelExtensions.Companion.nextNamed

/**
 * Handler for targeting any function or method definition.
 */
open class AnyFunctionHandler : TSBasedHandler() {
    override val innerBlockTypes =
        setOf(
            "block", "function_body", "compound_statement", "body_statement", "statements", "binary_operator", "binary_expression",
            "statement_block", "infix_expression", "expression_statement", "brace_list", "binary"
        )
    override val targetTypes = setOf(
        "anonymous_initializer", "block_expression", "constructor_declaration", "defn_def", "delegate_declaration",
        "fn_declaration", "function_declaration", "function_definition", "function_item", "function_specification", "function_signature",
        "function_statement", "generator_function_declaration", "init_block", "local_function_declaration",
        "method", "method_declaration", "method_definition", "method_signature", "procedure_definition", "subroutine_declaration"
    )

    override fun findSelection(editor: VimEditor, inner: Boolean, size: Int): TextRange? {
        val tsDocument = getTSDocument(editor)
        if (tsDocument.languageInfo != DART)
            return super.findSelection(editor, inner, size)
        // Dart needs special handling for functions due to the weird way Tree-sitter parses them.
        var body = tsDocument.findSelectionNode({ n -> n.grammarType == "function_body" })?.takeIf { !it.isNull }
        val signature = tsDocument.findSelectionNode({ acceptNode(it, tsDocument) })?.takeIf { !it.isNull }
        if (body != null && (signature == null || signature.startByte > tsDocument.getCaretByteOffset())) {
            if (inner) return getCodeBlock(body, tsDocument)
            val signature = body.prevSibling.takeIf { n -> n.grammarType == "function_signature" || n.grammarType == "method_signature" } ?: return tsDocument.toTextRange(body)
            return tsDocument.toTextRange(signature, body)
        }
        if (signature == null) return null
        if (signature.grammarType == "function_expression") {
            body = signature.getFirstNamedChildWithGrammar(setOf("function_expression_body")) ?: return tsDocument.toTextRange(signature)
            if (inner) return getCodeBlock(body, tsDocument)
            return tsDocument.toTextRange(signature)
        }
        body = signature.nextNamed()?.takeIf { n -> n.grammarType == "function_body" } ?: return tsDocument.toTextRange(signature)
        if (inner) return getCodeBlock(body, tsDocument)
        return tsDocument.toTextRange(signature, body)
    }
}
