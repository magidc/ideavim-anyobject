package com.magidc.ideavim.anyobject.handlers

import com.maddyhome.idea.vim.api.VimEditor
import com.magidc.ideavim.anyobject.handlers.base.TextBasedHandler
import com.magidc.ideavim.anyobject.model.Selection
import kotlin.math.max

class AnyIndentBlockHandler : TextBasedHandler() {
    override fun findTextSelection(text: CharSequence, textOffset: Int, caretOffset: Int, isInner: Boolean): Selection {
        TODO("Not yet implemented")
    }

    override fun findSelection(editor: VimEditor, isInner: Boolean, size: Int): Selection {
        val lineNumber = editor.currentCaret().getLine()
        val lineIndentation = editor.getLineText(lineNumber).takeWhile { it.isWhitespace() }
        var fromLine = lineNumber
        while (true) {
            fromLine--
            if (fromLine < 0 || editor.getLineText(fromLine).takeWhile { it.isWhitespace() } != lineIndentation) {
                fromLine++
                break
            }
        }
        var toLine = lineNumber
        while (true) {
            toLine++
            if (toLine == editor.lineCount() || editor.getLineText(toLine).takeWhile { it.isWhitespace() } != lineIndentation) {
                toLine--
                break
            }
        }
        return Selection(
            editor.getLineStartOffset(if (isInner) fromLine else max(0, fromLine - 1)),
            editor.getLineEndOffset(toLine)
        )
    }

}