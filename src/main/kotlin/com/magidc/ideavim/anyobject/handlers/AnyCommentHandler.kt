package com.magidc.ideavim.anyobject.handlers

import com.maddyhome.idea.vim.common.TextRange
import com.magidc.ideavim.anyobject.handlers.base.TSBasedHandler
import com.magidc.ideavim.anyobject.utils.TSDocument
import com.magidc.ideavim.anyobject.utils.TSModelExtensions.Companion.toText
import org.treesitter.TSNode

/**
 * Handler for targeting various types of comments (line, block, doc comments).
 */
class AnyCommentHandler : TSBasedHandler() {

    data class CommentTags(
        val lineTags: List<String>,
        val blockTags: List<Pair<String, String>>
    )

    companion object {
        private val C_STYLE_BLOCK = listOf("/*" to "*/")
        private val MARKUP_BLOCK = listOf("<!--" to "-->")

        val commentTagsMap: Map<String, CommentTags> = mapOf(
            "JAVA" to CommentTags(listOf("//"), C_STYLE_BLOCK),
            "KOTLIN" to CommentTags(listOf("//"), C_STYLE_BLOCK),
            "DART" to CommentTags(listOf("///", "//"), C_STYLE_BLOCK),
            "CLOJURE" to CommentTags(listOf(";"), emptyList()),
            "SCALA" to CommentTags(listOf("//"), C_STYLE_BLOCK),
            "C#" to CommentTags(listOf("///", "//"), C_STYLE_BLOCK),
            "RUST" to CommentTags(listOf("///", "//!", "//"), C_STYLE_BLOCK),
            "RUBY" to CommentTags(listOf("#"), listOf("=begin" to "=end")),
            "GO" to CommentTags(listOf("//"), C_STYLE_BLOCK),
            "PYTHON" to CommentTags(listOf("#"), listOf("\"\"\"" to "\"\"\"", "'''" to "'''")),
            "PHP" to CommentTags(listOf("//", "#"), C_STYLE_BLOCK),
            "HTML" to CommentTags(emptyList(), MARKUP_BLOCK),
            "CSS" to CommentTags(emptyList(), C_STYLE_BLOCK),
            "ECMAScript 6" to CommentTags(listOf("//"), C_STYLE_BLOCK),
            "TYPESCRIPT" to CommentTags(listOf("//"), C_STYLE_BLOCK),
            "OBJECTIVE-C" to CommentTags(listOf("//"), C_STYLE_BLOCK),
            "SWIFT" to CommentTags(listOf("///", "//"), C_STYLE_BLOCK),
            "C/C++" to CommentTags(listOf("//"), C_STYLE_BLOCK),
            "R" to CommentTags(listOf("#"), emptyList()),
            "SQL" to CommentTags(listOf("--", "#"), C_STYLE_BLOCK),
            // Standard JSON has no comments; JSONC tags are listed
            "JSON" to CommentTags(listOf("//"), C_STYLE_BLOCK),
            "XML" to CommentTags(emptyList(), MARKUP_BLOCK),
            "YAML" to CommentTags(listOf("#"), emptyList())
        )
    }

    override val targetTypes = setOf(
        "comment", "line_comment", "block_comment", "multiline_comment", "documentation_comment", "doc_comment", "javadoc", "kdoc", "phpdoc",
        "rustdoc", "attribute_comment", "hash_comment", "shell_comment", "marginalia"
    )

    override fun findInnerBlockRange(currentNode: TSNode, objectNode: TSNode, byteOffset: Int, tsDocument: TSDocument): TextRange {
        val outerRange = tsDocument.toTextRange(objectNode)
        val commentTags = commentTagsMap[tsDocument.languageInfo.name] ?: return outerRange
        val outerContent = objectNode.toText(editor = tsDocument.editor, Int.MAX_VALUE)
        commentTags.blockTags.firstOrNull { outerContent.startsWith(it.first) && outerContent.endsWith(it.second) }?.let {
            return TextRange(
                outerRange.startOffset - outerContent.indexOf(it.first) + it.first.length,
                outerRange.endOffset - (outerContent.length - outerContent.lastIndexOf(it.second))
            )
        }

        commentTags.lineTags.firstOrNull { outerContent.startsWith(it) }?.let {
            return TextRange(
                outerRange.startOffset + outerContent.indexOf(it) + it.length,
                outerRange.endOffset
            )
        }

        return outerRange
    }

}