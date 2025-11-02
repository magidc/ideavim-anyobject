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

    override fun findTextSelection(text: CharSequence, textOffset: Int, caretOffset: Int, isInner: Boolean, size: Int): TextRange? {
        return if (isInner) findInnerTextSelection(text, caretOffset) else findOuterTextSelection(text, caretOffset, size)
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

    private fun findOuterTextSelection(text: CharSequence, caretOffset: Int, size: Int): TextRange? {
        var firstRange: IntRange? = null
        var lastRange: IntRange? = null
        var count = 0
        for (m in outerSelectionRegex.findAll(text)) {
            val range = m.range
            if (caretOffset in range) {
                if (null == firstRange)
                    firstRange = range
                lastRange = range
                count = 1
            } else if (count > 0) {
                count++
                lastRange = range
            }
            if (count == size) break
        }
        if (null == firstRange || null == lastRange) return null
        return TextRange(firstRange.first, lastRange.last + 1)
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