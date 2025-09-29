package com.magidc.ideavim.anyobject.handlers

import com.magidc.ideavim.anyobject.handlers.base.DelimiterHandler

class AnyBlockCommentHandler : DelimiterHandler(false, delimiters) {
    companion object {
        private val delimiters = listOf(
            "/**" to "*/",          // Java
            "/*" to "*/",           // C, C++, Java, C#, JavaScript, TypeScript, Kotlin, Scala, Swift, Go, Rust, PHP
            "\"\"\"" to "\"\"\"",   // Python triple-quoted strings
            "'''" to "'''",         // Python triple-quoted strings (single quotes)
            "<!--" to "-->",        // HTML, XML, XHTML, Markdown
            "--[[" to "]]",         // Lua
            "--[=[" to "]=]",       // Lua (with custom delimiters)
            "#'" to "'",            // R (roxygen comments, though not exactly block comments)
            "{-" to "-}",           // Haskell
        )
    }
}