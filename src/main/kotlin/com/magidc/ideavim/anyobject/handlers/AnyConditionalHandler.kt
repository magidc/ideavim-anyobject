package com.magidc.ideavim.anyobject.handlers

import com.maddyhome.idea.vim.common.TextRange
import com.magidc.ideavim.anyobject.handlers.base.TSBasedHandler
import com.magidc.ideavim.anyobject.utils.TSDocument
import com.magidc.ideavim.anyobject.utils.TSLanguageUtils.Companion.CPP
import com.magidc.ideavim.anyobject.utils.TSLanguageUtils.Companion.JAVA
import com.magidc.ideavim.anyobject.utils.TSLanguageUtils.Companion.PHP
import com.magidc.ideavim.anyobject.utils.TSLanguageUtils.Companion.PYTHON
import com.magidc.ideavim.anyobject.utils.TSLanguageUtils.Companion.RUBY
import com.magidc.ideavim.anyobject.utils.TSModelExtensions.Companion.getFirstNamedChildWithGrammar
import org.treesitter.TSNode

/**
 * Handler for targeting any conditional statement like if, switch, match, or try-catch blocks.
 */
class AnyConditionalHandler : TSBasedHandler() {

    override val innerBlockTypes = setOf(
        "switch_block_statement_group", "switch_section", "case_statement", "block", "else", "except_clause", "when",
        "return_statement", "when_entry", "match_arm", "default_statement", "case_clause", "else_clause", "braced_expression",
        "statement_block", "switch_case", "then", "switch_default", "statements", "switch_entry", "default_case", "expression_case",
        "switch_statement_case"
    )

    override val targetTypes = setOf(
        "match_statement", "match_expression", "switch_statement", "switch_expression", "when_expression", "begin", "case", "try_expression",
        "try_statement", "if_statement", "if_expression", "unless_statement", "branch_expression", "try_with_resources_statement", "do_statement",
        "expression_switch_statement", "if"
    )
    private val blockType = setOf("block")
    private val expressionStatementType = setOf("expression_statement")
    private val compoundStatementType = setOf("compound_statement")
    private val callType = setOf("call")
    private val pythonInnerBlockTypes = innerBlockTypes - blockType

    private fun findRubyInnerConditionalBlockRange(objectNode: TSNode, offset: Int, tsDocument: TSDocument): TextRange? {
        val types = when (objectNode.grammarType) {
            "if" -> callType
            else -> innerBlockTypes
        }
        return objectNode.getFirstNamedChildWithGrammar(types, offset)
            ?.takeIf { it.namedChildCount > 0 }
            ?.let { tsDocument.toTextRange(it) }
    }

    private fun findCppPhpInnerConditionalBlockRange(objectNode: TSNode, offset: Int, tsDocument: TSDocument): TextRange? {
        val types = when (objectNode.grammarType) {
            "if_statement" -> compoundStatementType
            "try_statement" -> compoundStatementType
            else -> innerBlockTypes
        }
        return objectNode.getFirstNamedChildWithGrammar(types, offset)
            ?.takeIf { it.namedChildCount > 0 }
            ?.let { getCodeBlock(it, tsDocument) }
    }

    private fun findJavaInnerConditionalBlockRange(currentNode: TSNode, objectNode: TSNode, offset: Int, tsDocument: TSDocument): TextRange? {
        if (objectNode.grammarType == "if_statement") {
            objectNode.getFirstNamedChildWithGrammar(innerBlockTypes, offset)
                ?.takeIf { it.namedChildCount > 0 }
                ?.let { getBracesCodeBlock(it, tsDocument) }
                ?.let { return it }
            return objectNode.getFirstNamedChildWithGrammar(expressionStatementType, offset)
                ?.takeIf { it.namedChildCount > 0 }
                ?.let { tsDocument.toTextRange(it) }
                ?.let { return it }
        }
        return super.findInnerBlockRange(currentNode, objectNode, offset, tsDocument)
    }

    private fun findPythonInnerConditionalBlockRange(objectNode: TSNode, offset: Int, tsDocument: TSDocument): TextRange? {
        val types = when (objectNode.grammarType) {
            "if_statement" -> blockType
            "try_statement" -> blockType
            else -> pythonInnerBlockTypes
        }
        return objectNode.getFirstNamedChildWithGrammar(types, offset)
            ?.takeIf { it.namedChildCount > 0 }
            ?.let { tsDocument.toTextRange(it) }
    }

    override fun findInnerBlockRange(currentNode: TSNode, objectNode: TSNode, offset: Int, tsDocument: TSDocument): TextRange? {
        return when (tsDocument.languageInfo.name) {
            PYTHON.name -> findPythonInnerConditionalBlockRange(objectNode, offset, tsDocument)
            JAVA.name -> findJavaInnerConditionalBlockRange(currentNode, objectNode, offset, tsDocument)
            CPP.name -> findCppPhpInnerConditionalBlockRange(objectNode, offset, tsDocument)
            PHP.name -> findCppPhpInnerConditionalBlockRange(objectNode, offset, tsDocument)
            RUBY.name -> findRubyInnerConditionalBlockRange(objectNode, offset, tsDocument)
            else -> super.findInnerBlockRange(currentNode, objectNode, offset, tsDocument) ?: tsDocument.toTextRange(objectNode)
        }
    }
}
