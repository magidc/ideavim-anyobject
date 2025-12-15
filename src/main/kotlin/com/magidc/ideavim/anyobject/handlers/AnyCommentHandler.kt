package com.magidc.ideavim.anyobject.handlers

import com.magidc.ideavim.anyobject.handlers.base.TSBasedHandler

class AnyCommentHandler : TSBasedHandler() {
    override val targetTypes = setOf(
        "comment", "line_comment", "block_comment", "multiline_comment", "documentation_comment", "doc_comment", "javadoc", "kdoc", "phpdoc",
        "rustdoc", "attribute_comment", "hash_comment", "shell_comment"
    )
}