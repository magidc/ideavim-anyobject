package com.magidc.ideavim.anyobject.handlers

import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.command.TextObjectVisualType
import com.maddyhome.idea.vim.common.TextRange
import com.magidc.ideavim.anyobject.handlers.base.TextBasedHandler

/**
 * Handler for targeting an indented block of code.
 */
class AnyIndentBlockHandler : TextBasedHandler() {
    override fun findTextSelection(text: CharSequence, textOffset: Int, caretOffset: Int, isInner: Boolean, size: Int): TextRange {
        TODO("Not yet implemented")
    }

    override fun getVisualType(inner: Boolean): TextObjectVisualType {
        return if (inner) TextObjectVisualType.CHARACTER_WISE else TextObjectVisualType.LINE_WISE
    }

    override fun findSelection(editor: VimEditor, inner: Boolean, size: Int): TextRange {
        var lineNumber = editor.currentCaret().getLine()
        while (lineNumber > 0 && editor.getLineText(lineNumber).isBlank()) lineNumber--
        val lineIndentation = editor.getLineText(lineNumber).takeWhile { it.isWhitespace() }.length
        var fromLine = lineNumber
        while (fromLine > 0) {
            fromLine--
            val lineText = editor.getLineText(fromLine)
            if (lineText.isNotBlank() && lineText.takeWhile { it.isWhitespace() }.length < lineIndentation) {
                fromLine++
                break
            }
        }
        var toLine = lineNumber
        while (toLine < editor.lineCount() - 1) {
            toLine++
            val lineText = editor.getLineText(toLine)
            if (lineText.isNotBlank() && lineText.takeWhile { it.isWhitespace() }.length < lineIndentation) {
                toLine--
                break
            }
        }
        while (toLine < editor.lineCount() && editor.getLineText(toLine).isBlank()) toLine--
        while (fromLine > 0 && editor.getLineText(fromLine).isBlank()) fromLine++
        return TextRange(
            editor.getLineStartOffset(fromLine) + lineIndentation,
            editor.getLineEndOffset(toLine)
        )
    }
}