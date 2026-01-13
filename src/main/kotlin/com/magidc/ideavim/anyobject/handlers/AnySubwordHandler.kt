package com.magidc.ideavim.anyobject.handlers

import andel.intervals.toReversedList
import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.common.TextRange
import com.magidc.ideavim.anyobject.handlers.base.BaseJumpHandler
import com.magidc.ideavim.anyobject.handlers.base.TextBasedHandler
import com.magidc.ideavim.anyobject.handlers.base.getCaretOffset


/**
 * Handler for targeting subwords within camelCase or snake_case identifiers.
 */
class AnySubwordHandler : TextBasedHandler(), BaseJumpHandler {
    private companion object {
        // It naturally ignores - and _ since those characters are not part of the token patterns, so dash-case and snake_case are split into tokens on those separators.
        val innerSelectionRegex = Regex("""[A-Z]?[a-z]+|[A-Z]+(?![a-z])|\d+""")
        val outerSelectionRegex = Regex("""(?:[A-Z]?[a-z]+|[A-Z]+(?![a-z])|\d+)[-_]?""")
        val wordSelectionRegex = Regex("[\\w-]+")
        val supportedDelimiters = setOf('-', '_')
    }

    override fun allowsCountSelection(): Boolean = true

    override fun findTextSelection(text: CharSequence, textOffset: Int, caretOffset: Int, isInner: Boolean, size: Int): TextRange? {
        return if (isInner) findInnerTextSelection(text, caretOffset, size) else findOuterTextSelection(text, caretOffset, size)
    }

    private fun findInnerTextSelection(text: CharSequence, caretOffset: Int, size: Int): TextRange? {
        val caretOnDelimiter = supportedDelimiters.contains(text[caretOffset])
        if (caretOnDelimiter && caretOffset == 0) return null
        // If caret is on delimiter, include it as part of subword before it
        val ranges = findSelectionRanges(text, if (caretOnDelimiter) caretOffset - 1 else caretOffset, size, innerSelectionRegex) ?: return null
        val firstRange: IntRange = ranges.first
        val lastRange: IntRange = ranges.second

        if (caretOnDelimiter && (caretOffset - 1 == lastRange.last)) return TextRange(firstRange.first, lastRange.last + 1)
        return TextRange(firstRange.first, lastRange.last + 1)
    }

    private fun findOuterTextSelection(text: CharSequence, caretOffset: Int, size: Int): TextRange? {
        val ranges = findSelectionRanges(text, caretOffset, size, outerSelectionRegex) ?: return null
        val firstRange: IntRange = ranges.first
        val lastRange: IntRange = ranges.second
        return TextRange(firstRange.first, lastRange.last + 1)
    }

    override fun findJumpElement(editor: VimEditor, forward: Boolean): TextRange? {
        val caretOffset = editor.getCaretOffset()
        val sequence = outerSelectionRegex.findAll(editor.text())
        return (if (forward) sequence.firstOrNull { it.range.first > caretOffset } else sequence.toReversedList().firstOrNull { it.range.last < caretOffset })
            ?.let { TextRange(it.range.first, it.range.last + 1) }
    }

    private fun findWordRange(text: CharSequence, caretOffset: Int): IntRange? = wordSelectionRegex.findAll(text).find { caretOffset in it.range }?.range

    private fun findSelectionRanges(text: CharSequence, caretOffset: Int, size: Int, regex: Regex): Pair<IntRange, IntRange>? {
        var firstRange: IntRange? = null
        var lastRange: IntRange? = null
        val wordRange = findWordRange(text, caretOffset) ?: return null
        var count = 0
        for (m in regex.findAll(text)) {
            val range = m.range
            if (range.first < wordRange.first) continue
            if (range.first > wordRange.last) break
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
        return Pair(firstRange, lastRange)
    }
}