package com.magidc.ideavim.anyobject.handlers

import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.common.TextRange
import com.magidc.ideavim.anyobject.handlers.base.TSBasedHandler
import com.magidc.ideavim.anyobject.utils.TSDocument
import com.magidc.ideavim.anyobject.utils.TSModelExtensions.Companion.getFirstChildWithGrammar
import com.magidc.ideavim.anyobject.utils.TSModelExtensions.Companion.nextLeaf
import org.treesitter.TSNode
import org.treesitter.TreeSitterJavascript
import org.treesitter.TreeSitterTypescript
import kotlin.reflect.KClass

/**
 * Handler for targeting variable and property declarations or assignments.
 */
class AnyVariableHandler : TSBasedHandler() {
    override val targetTypes = setOf(
        "assignment", "assignment_expression", "property_declaration", "field_declaration", "field_definition", "local_declaration_statement",
        "local_variable_declaration", "declaration", "public_field_definition", "lexical_declaration",
        "val_definition", "var_definition", "let_declaration", "short_var_declaration", "var_declaration",
        "property_declaration"
    )
    override val languageTargetTypes: Map<KClass<*>, Set<String>> = mapOf(
        TreeSitterJavascript::class to setOf("variable_declaration"),
        TreeSitterTypescript::class to setOf("variable_declaration")
    )

    override fun findSelection(editor: VimEditor, inner: Boolean, size: Int): TextRange? {
        val tsDocument = getTSDocument(editor)
        val objectNode = tsDocument.findSelectionNode({ acceptNode(it, tsDocument) }) ?: return null
        val objectNodes = sequenceOf(objectNode, objectNode.nextSibling.takeIf { !it.isNull && it.grammarType == ";" })
            .filterNotNull().toList()
        if (!inner) return tsDocument.toTextRange(objectNodes.first(), objectNodes.last())
        return findValueAssigmentBlockRange(objectNodes, tsDocument)
    }

    private fun findValueAssigmentBlockRange(nodes: List<TSNode>, tsDocument: TSDocument): TextRange {
        val first = nodes.first()
        val fromNode = first.getFirstChildWithGrammar(setOf("=", ":="))?.nextLeaf() ?: first
        return tsDocument.toTextRange(fromNode)
    }
}