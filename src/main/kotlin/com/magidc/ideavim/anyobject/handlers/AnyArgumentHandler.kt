package com.magidc.ideavim.anyobject.handlers

import com.maddyhome.idea.vim.common.TextRange
import com.magidc.ideavim.anyobject.utils.TSDocument
import org.treesitter.TSNode

/**
 * Handler for targeting any argument or parameter in a function or method.
 */
open class AnyArgumentHandler : AnyItemHandler() {
    companion object {
        val targetTypes = setOf("argument", "parameter", "formal_parameter", "element_value_pair")
        val parentTargetTypes = setOf(
            "arguments", "argument_list", "value_arguments", "formal_parameters", "formal_parameters_list",
            "parameters", "parameter_list", "function_parameters", "primary_constructor", "function_value_parameters",
            "lambda_parameters", "lambda_parameter_list", "array_creation_expression", "method_parameters", "attribute_argument_list"
        )
    }

    override fun findInnerBlockRange(currentNode: TSNode, objectNode: TSNode, byteOffset: Int, tsDocument: TSDocument): TextRange {
        if (objectNode.grammarType == "element_value_pair" && objectNode.childCount >= 3)
            objectNode.getChild(2)?.let { return tsDocument.toTextRange(it) }
        return super.findInnerBlockRange(currentNode, objectNode, byteOffset, tsDocument)
    }

    override val parentTargetTypes = Companion.parentTargetTypes
    override val targetTypes = Companion.targetTypes
}
