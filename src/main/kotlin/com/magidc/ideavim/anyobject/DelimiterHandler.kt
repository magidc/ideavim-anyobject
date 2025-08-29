package com.magidc.ideavim.anyObject

import com.maddyhome.idea.vim.api.ExecutionContext
import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.command.OperatorArguments
import com.maddyhome.idea.vim.extension.ExtensionHandler
import com.maddyhome.idea.vim.state.mode.Mode
import com.maddyhome.idea.vim.state.mode.SelectionType
import com.magidc.ideavim.anyObject.model.Selection

/**
 * Applies the action (pending operation or visual selection) to the smallest range of text around the cursor limited by any of the given delimiter pairs.
 */
class DelimiterHandler(val isInner: Boolean, val sameLine: Boolean, val delimiterPairs: Collection<Pair<String, String>>) : ExtensionHandler {
    fun findSelection(text: CharSequence, textOffset: Int, caretOffset: Int): Selection? {
        var bestMatch: Selection? = null
        var bestMatchLength = Int.MAX_VALUE

        for (delimiterPair in delimiterPairs) {
            val openDelimiter = delimiterPair.first
            val closeDelimiter = delimiterPair.second

            val match =
                if (openDelimiter == closeDelimiter)
                    findByDelimiter(text, openDelimiter, caretOffset, bestMatchLength)
                else
                    findByDelimiters(text, openDelimiter, closeDelimiter, caretOffset, bestMatchLength)

            if (null == match)
                continue

            bestMatchLength = match.last - match.first

            bestMatch = if (isInner)
                Selection(
                    textOffset + match.first,
                    textOffset + match.last,
                    openDelimiter,
                    closeDelimiter
                )
            else
                Selection(
                    textOffset + match.first - openDelimiter.length,
                    textOffset + match.last + closeDelimiter.length,
                    openDelimiter,
                    closeDelimiter
                )
        }
        return bestMatch
    }

    override fun execute(editor: VimEditor, context: ExecutionContext, operatorArguments: OperatorArguments) {
        val caret = editor.currentCaret()
        val textOffset = if (sameLine) editor.getLineRange(caret.getLine()).first else 0
        val text = if (sameLine) editor.getLineText(caret.getLine()) else editor.text()

        val caretOffset = caret.offset - textOffset
        val bestMatch = findSelection(text, textOffset, caretOffset) ?: return

        if (editor.mode is Mode.OP_PENDING) {
            val mode = editor.mode as Mode.OP_PENDING
            caret.moveToOffset(bestMatch.to - 1)
            editor.mode = Mode.VISUAL(SelectionType.CHARACTER_WISE)
            caret.setSelection(bestMatch.from, bestMatch.to)
            editor.mode = mode
        } else {
            caret.moveToOffset(bestMatch.to - 1)
            caret.setSelection(bestMatch.from, bestMatch.to)
        }
    }

    /**
     * Finds the smallest range of text around the caret limited by the given delimiter.
     * The range cannot exceed the best match length.
     * Returns an INNER range.
     */

    private fun findByDelimiter(text: CharSequence, delimiter: String, caretOffset: Int, bestMatchLength: Int): IntRange? {
        // Scenario where the caret is in a delimiter. We don't know if the other delimiter is before or after the caret.
        if (text.substring(caretOffset).startsWith(delimiter)) {
            val delimiterAfter = text.indexOf(delimiter, caretOffset + 1)
            var delimiterBefore = text.lastIndexOf(delimiter, caretOffset - 1)
            if (delimiterBefore == -1 && delimiterAfter == -1)
                return null
            if (delimiterBefore != -1 && delimiterAfter != -1) {
                delimiterBefore += delimiter.length
                if ((caretOffset - delimiterBefore) < (delimiterAfter - caretOffset))
                    return IntRange(delimiterBefore, caretOffset)
                return IntRange(caretOffset + delimiter.length, delimiterAfter)
            }
            if (delimiterAfter == -1)
                return IntRange(delimiterBefore + delimiter.length, caretOffset)
            return IntRange(caretOffset + delimiter.length, delimiterAfter)
        }
        // Regular scenario where the caret is not in a delimiter.
        else {
            val delimiterAfter = text.indexOf(delimiter, caretOffset)
            val delimiterBefore = text.lastIndexOf(delimiter, caretOffset)
            if (delimiterBefore != -1 && delimiterAfter != -1 && (delimiterAfter - delimiterBefore) < bestMatchLength)
                return IntRange(delimiterBefore + delimiter.length, delimiterAfter)
        }
        return null
    }

    /**
     * Finds the smallest range of text around the caret limited by any of the given delimiters.
     * The range cannot exceed the best match length.
     * Returns an INNER range.
     */
    private fun findByDelimiters(text: CharSequence, openDelimiter: String, closeDelimiter: String, caretOffset: Int, bestMatchLength: Int): IntRange? {
        val textFromCaret = text.substring(caretOffset)

        if (textFromCaret.startsWith(closeDelimiter)) {
            val openDelimiterOffset = findOpenDelimiter(text.take(caretOffset), openDelimiter, closeDelimiter, bestMatchLength)
            return if (openDelimiterOffset != -1) IntRange(openDelimiterOffset + openDelimiter.length, caretOffset) else null
        }
        if (textFromCaret.startsWith(openDelimiter)) {
            val closingOffset = findClosingDelimiter(textFromCaret.substring(openDelimiter.length), openDelimiter, closeDelimiter, bestMatchLength)
            return if (closingOffset != -1) IntRange(caretOffset + openDelimiter.length, closingOffset + 1 + caretOffset) else null
        }
        val closingOffset = findClosingDelimiter(textFromCaret, openDelimiter, closeDelimiter, bestMatchLength)
        if (closingOffset == -1)
            return null
        val openDelimiterOffset = findOpenDelimiter(text.take(caretOffset), openDelimiter, closeDelimiter, bestMatchLength - closingOffset)
        if (openDelimiterOffset == -1)
            return null
        val range = IntRange(openDelimiterOffset + openDelimiter.length, closingOffset + caretOffset)
        return if ((range.last - range.first) < bestMatchLength) range else null
    }

    /**
     * Find the first open delimiter backwards from the end of the text. It takes into consideration closing delimiters found before.
     * The range cannot exceed the best match length.
     */
    private fun findOpenDelimiter(text: CharSequence, openDelimiter: String, closeDelimiter: String, bestMatchLength: Int): Int {
        val allMatches = (findAllMatches(text, openDelimiter) + findAllMatches(text, closeDelimiter))
            .sortedByDescending { it.range.first }
        var level = 0
        for (match in allMatches) {
            if (match.range.first > bestMatchLength)
                return -1
            if (match.value == openDelimiter) {
                if (level == 0)
                    return match.range.first
                level--
            } else
                level++
        }
        return -1
    }

    /**
     * Find the first closing delimiter forwards from the start of the text. It takes into consideration opening delimiters found before.
     * The range cannot exceed the best match length.
     */
    private fun findClosingDelimiter(text: CharSequence, openDelimiter: String, closeDelimiter: String, bestMatchLength: Int): Int {
        val allMatches = (findAllMatches(text, openDelimiter) + findAllMatches(text, closeDelimiter))
            .sortedBy { it.range.first }

        var level = 0
        for (match in allMatches) {
            if (match.range.first > bestMatchLength)
                return -1
            if (match.value == closeDelimiter) {
                if (level == 0)
                    return match.range.first
                level--
            } else
                level++
        }
        return -1
    }

    private fun findAllMatches(text: CharSequence, searchString: String): List<MatchResult> {
        val regex = Regex.escape(searchString).toRegex()
        return regex.findAll(text).toList()
    }
}
