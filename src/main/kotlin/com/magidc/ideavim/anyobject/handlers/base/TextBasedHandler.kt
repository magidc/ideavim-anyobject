package com.magidc.ideavim.anyobject.handlers.base

import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.common.TextRange

/**
 * Base class for all handlers that are based on raw text analysis
 */
abstract class TextBasedHandler() : BaseSelectionHandler {
    abstract fun findTextSelection(text: CharSequence, textOffset: Int, caretOffset: Int, isInner: Boolean, size: Int = 1): TextRange?

    override fun findSelection(editor: VimEditor, isInner: Boolean, size: Int): TextRange? {
        val caret = editor.currentCaret()
        return findTextSelection(editor.text(), 0, caret.offset, isInner, size)
    }
}

/**
 * Handler that operates on text delimited by a pair of delimiters.
 */
open class DelimiterHandler(val sameLine: Boolean, val delimiterPairs: Collection<Pair<String, String>>) : TextBasedHandler() {

    override fun findSelection(editor: VimEditor, isInner: Boolean, size: Int): TextRange? {
        val caret = editor.currentCaret()
        val textOffset = if (sameLine) editor.getLineRange(caret.getLine()).first else 0
        val text = if (sameLine) editor.getLineText(caret.getLine()) else editor.text()

        val caretOffset = caret.offset - textOffset
        return findTextSelection(text, textOffset, caretOffset, isInner)
    }

    override fun findTextSelection(text: CharSequence, textOffset: Int, caretOffset: Int, isInner: Boolean, size: Int): TextRange? {
        if (text.isEmpty()) return null
        var bestMatch: TextRange? = null
        var bestMatchLength = Int.MAX_VALUE
        val presentDelimiters = mutableSetOf<Pair<String, String>>()

        // First, trying to match assuming the caret is within delimiters
        for (delimiterPair in delimiterPairs) {
            val openDelimiter = delimiterPair.first
            val closeDelimiter = delimiterPair.second
            val equalDelimiters = openDelimiter == closeDelimiter

            if (!text.contains(openDelimiter) || (!equalDelimiters && !text.contains(closeDelimiter))) continue
            presentDelimiters.add(delimiterPair)

            val match =
                if (equalDelimiters)
                    findByDelimiter(text, openDelimiter, caretOffset, bestMatchLength)
                else
                    findByDelimiters(text, openDelimiter, closeDelimiter, caretOffset, bestMatchLength)

            if (null == match) continue

            bestMatchLength = match.last - match.first

            bestMatch =
                if (isInner)
                    TextRange(textOffset + match.first, textOffset + match.last)
                else
                    TextRange(textOffset + match.first - openDelimiter.length, textOffset + match.last + closeDelimiter.length)
        }
        if (null != bestMatch || presentDelimiters.isEmpty()) return bestMatch

        // Second, trying to match assuming the caret is before the first delimiter
        var nearestDelimitersToCaret: Pair<String, String>? = null
        var nearestOpenDelimiterOffset = Int.MAX_VALUE
        var nearestCloseDelimiterOffset = 0

        // Checking which pair of delimiters is closer to the caret
        for (delimiterPair in presentDelimiters) {
            val openDelimiterOffset = text.indexOf(delimiterPair.first, caretOffset)
            if (openDelimiterOffset == -1 || openDelimiterOffset > nearestOpenDelimiterOffset) continue

            val closeDelimiterOffset = text.indexOf(delimiterPair.second, openDelimiterOffset + 1)
            if (closeDelimiterOffset == -1) continue

            if (openDelimiterOffset < nearestOpenDelimiterOffset) {
                nearestDelimitersToCaret = delimiterPair
                nearestOpenDelimiterOffset = openDelimiterOffset
                nearestCloseDelimiterOffset = closeDelimiterOffset
                if (nearestOpenDelimiterOffset == 1) break
            }
        }

        if (nearestDelimitersToCaret == null) return null

        return if (isInner)
            TextRange(
                textOffset + nearestOpenDelimiterOffset + nearestDelimitersToCaret.first.length,
                textOffset + nearestCloseDelimiterOffset
            )
        else
            TextRange(
                textOffset + nearestOpenDelimiterOffset,
                textOffset + nearestCloseDelimiterOffset + nearestDelimitersToCaret.second.length,
            )
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
            val openDelimiterOffset = findOpenDelimiter(text.take(caretOffset), openDelimiter, closeDelimiter, caretOffset, bestMatchLength)
            return if (openDelimiterOffset != -1) IntRange(openDelimiterOffset + openDelimiter.length, caretOffset) else null
        }
        if (textFromCaret.startsWith(openDelimiter)) {
            val closingOffset = findClosingDelimiter(textFromCaret.substring(openDelimiter.length), openDelimiter, closeDelimiter, bestMatchLength)
            return if (closingOffset != -1) IntRange(caretOffset + openDelimiter.length, closingOffset + 1 + caretOffset) else null
        }
        var closingOffset = findClosingDelimiter(textFromCaret, openDelimiter, closeDelimiter, bestMatchLength)
        if (closingOffset == -1)
            return null
        closingOffset += caretOffset
        val openDelimiterOffset = findOpenDelimiter(text.take(caretOffset), openDelimiter, closeDelimiter, closingOffset, bestMatchLength)
        if (openDelimiterOffset == -1)
            return null
        val range = IntRange(openDelimiterOffset + openDelimiter.length, closingOffset)
        return if ((range.last - range.first) < bestMatchLength) range else null
    }

    /**
     * Find the first open delimiter backwards from the end of the text. It takes into consideration closing delimiters found before.
     * The range cannot exceed the best match length.
     */
    private fun findOpenDelimiter(text: CharSequence, openDelimiter: String, closeDelimiter: String, closeDelimiterOffset: Int, bestMatchLength: Int): Int {
        val allMatches = findAllMatches(text, listOf(openDelimiter, closeDelimiter)).sortedByDescending { it.range.first }
        var level = 0
        for (match in allMatches) {
            if (closeDelimiterOffset - match.range.first > bestMatchLength)
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
        val allMatches = findAllMatches(text, listOf(openDelimiter, closeDelimiter)).sortedBy { it.range.first }

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

    private fun findAllMatches(text: CharSequence, searchStrings: Collection<String>): List<MatchResult> {
        val regex = searchStrings.joinToString("|") { Regex.escape(it) }.toRegex()
        return regex.findAll(text).toList()
    }
}