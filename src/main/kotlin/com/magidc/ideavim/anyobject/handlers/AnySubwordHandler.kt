package com.magidc.ideavim.anyobject.handlers

import andel.intervals.toReversedList
import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.common.TextRange
import com.magidc.ideavim.anyobject.handlers.base.BaseJumpHandler
import com.magidc.ideavim.anyobject.handlers.base.TextBasedHandler


class AnySubwordHandler : TextBasedHandler(), BaseJumpHandler {
    private companion object {
        // It naturally ignores - and _ since those characters are not part of the token patterns, so dash-case and snake_case are split into tokens on those separators.
        val innerSelectionRegex = Regex("""[A-Z]?[a-z]+|[A-Z]+(?![a-z])|\d+""")
        val outerSelectionRegex = Regex("""(?:[A-Z]?[a-z]+|[A-Z]+(?![a-z])|\d+)[-_]?""")
        val supportedDelimiters = setOf('-', '_')
    }

    override fun findTextSelection(text: CharSequence, textOffset: Int, caretOffset: Int, isInner: Boolean): TextRange? {
        return if (isInner) findInnerTextSelection(text, caretOffset) else findOuterTextSelection(text, caretOffset)
    }

    private fun findInnerTextSelection(text: CharSequence, caretOffset: Int): TextRange? {
        val caretOnDelimiter = supportedDelimiters.contains(text[caretOffset])
        for (m in innerSelectionRegex.findAll(text)) {
            val range = m.range
            // If caret is on delimiter, include it as part of subword before it
            if (caretOnDelimiter && (caretOffset - 1 == range.last)) return TextRange(range.first, range.last + 1)
            if (caretOffset in range) return TextRange(range.first, range.last + 1)
        }
        return null
    }

    private fun findOuterTextSelection(text: CharSequence, caretOffset: Int): TextRange? {
        for (m in outerSelectionRegex.findAll(text)) {
            if (caretOffset in m.range) return TextRange(m.range.first, m.range.last + 1)
        }
        return null
    }

    override fun findJumpElementStartOffset(editor: VimEditor, next: Boolean): Int? {
        val caretOffset = editor.currentCaret().offset
        var found = false
        val sequence = outerSelectionRegex.findAll(editor.text())
        val matches = if (next) sequence.toList() else sequence.toReversedList()
        for (m in matches) {
            if (found) return m.range.first
            if (caretOffset in m.range)
                found = true
        }
        return null
    }
}