package com.magidc.ideavim.anyObject

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.magidc.ideavim.anyObject.handlers.BaseHandlers

abstract class BaseTest(val handlers: BaseHandlers) : BasePlatformTestCase() {
    companion object {
        const val CARET = "#caret#"
        const val START = "#start#"
        const val END = "#end#"
    }

    protected fun testInner(text: String) {
        execute(text, true, handlers.getInnerHandler())
    }

    protected fun testOuter(text: String) {
        execute(text, false, handlers.getOuterHandler())
    }

    private fun execute(textWithCaret: String, inner: Boolean, delimiterHandler: DelimiterHandler) {
        val caretIndex = textWithCaret.indexOf(CARET)
        val text = textWithCaret.replace(CARET, "")

        if (caretIndex == -1) return

        val selection = delimiterHandler.findSelection(text, 0, caretIndex)
        if (selection == null) {
            assertTrue(!text.contains(START) && !text.contains(END))
            return
        }

        val selectedText = if (inner)
            text.substring(selection.from, selection.to)
        else
            text.substring(selection.from - START.length, selection.to + END.length)

        assertTrue(selectedText, selectedText.startsWith(START))
        assertTrue(selectedText, selectedText.endsWith(END))
    }

}
