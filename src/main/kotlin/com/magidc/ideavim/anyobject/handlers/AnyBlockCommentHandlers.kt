package com.magidc.ideavim.anyobject.handlers

import com.magidc.ideavim.anyobject.handlers.base.DelimiterHandler
import com.magidc.ideavim.anyobject.handlers.base.DelimiterHandlerFactory

class AnyBlockCommentHandlers : DelimiterHandlerFactory {
    companion object {
        private val delimiters = listOf(
            "/**" to "*/",          // Java
            "/*" to "*/",           // C, C++, Java, C#, JavaScript, TypeScript, Kotlin, Scala, Swift, Go, Rust, PHP
            "\"\"\"" to "\"\"\"",   // Python triple-quoted strings
            "'''" to "'''",         // Python triple-quoted strings (single quotes)
            "<!--" to "-->",        // HTML, XML, XHTML, Markdown
            "{" to "}",             // Pascal, Delphi (alternative style)
            "(*" to "*)",           // Pascal, Delphi, OCaml, F#, AppleScript
            "%{" to "%}",           // MATLAB, Octave
            "--[[" to "]]",         // Lua
            "--[=[" to "]=]",       // Lua (with custom delimiters)
            "<#" to "#>",           // PowerShell
            "#'" to "'",            // R (roxygen comments, though not exactly block comments)
            "!*" to "*!",           // Some Fortran variants
            "{-" to "-}",           // Haskell
        )
    }

    override fun getInnerHandler(): DelimiterHandler {
        return DelimiterHandler(isInner = true, sameLine = false, delimiterPairs = delimiters)
    }

    override fun getOuterHandler(): DelimiterHandler {
        return DelimiterHandler(isInner = false, sameLine = false, delimiterPairs = delimiters)
    }
}