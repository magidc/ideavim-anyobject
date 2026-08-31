package com.magidc.ideavim.anyobject.handlers

import com.maddyhome.idea.vim.common.TextRange
import com.magidc.ideavim.anyobject.handlers.base.TSBasedHandler
import com.magidc.ideavim.anyobject.utils.TSDocument
import org.treesitter.TSNode

/**
 * Handler for targeting various types of comments (line, block, doc comments).
 */
class AnyCommentHandler : TSBasedHandler() {
    override val targetTypes = setOf(
        "comment", "line_comment", "block_comment", "multiline_comment", "documentation_comment", "doc_comment", "javadoc", "kdoc", "phpdoc",
        "rustdoc", "attribute_comment", "hash_comment", "shell_comment", "marginalia"
    )

    override fun findInnerBlockRange(currentNode: TSNode, objectNode: TSNode, byteOffset: Int, tsDocument: TSDocument): TextRange {
        return super.findInnerBlockRange(currentNode, objectNode, byteOffset, tsDocument) ?: tsDocument.toTextRange(objectNode)
    }

}