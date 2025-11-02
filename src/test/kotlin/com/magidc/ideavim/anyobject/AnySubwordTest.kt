package com.magidc.ideavim.anyobject

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.magidc.ideavim.anyobject.TextHandlerBaseTest.Companion.CARET
import com.magidc.ideavim.anyobject.TextHandlerBaseTest.Companion.END
import com.magidc.ideavim.anyobject.TextHandlerBaseTest.Companion.START
import com.magidc.ideavim.anyobject.handlers.AnySubwordHandler
import org.apache.commons.lang3.StringUtils

class AnySubwordTest : BasePlatformTestCase() {
    val handler = AnySubwordHandler()

    private fun executeTest(text: String, notApply: Boolean = false, inner: Boolean = true, size: Int = 1) {
        val cleanText = text.replace(START, "").replace(END, "")
        val caretOffset = cleanText.indexOf(CARET)
        val testInputText = cleanText.replace(CARET, "")
        val selection = handler.findTextSelection(testInputText, 0, caretOffset, inner, size)
        if (notApply) {
            assertNull(selection)
            return
        }
        assertNotNull(selection)
        val targetText = StringUtils.substringBetween(text, START, END).replace(CARET, "")
        val selectedText = testInputText.substring(selection!!.startOffset, selection.endOffset)
        assertEquals(targetText, selectedText)
    }

    fun testInnerCamelCase() {
        // Caret in the middle of the subword
        executeTest(String.format("this is %sf%soo%sBar", START, CARET, END))
        executeTest(String.format("this is foo%sB%sar%s", START, CARET, END))
        // Caret at the start of the subword
        executeTest(String.format("this is %s%sfoo%sBar", START, CARET, END))
        executeTest(String.format("this is foo%s%sBar%s", START, CARET, END))
        // Caret at the end of the subword
        executeTest(String.format("this is %sfo%so%sBar", START, CARET, END))
        executeTest(String.format("this is foo%sBa%sr%s", START, CARET, END))

        // Subword = Word
        executeTest(String.format("this is %sfooba%sr%s", START, CARET, END))

        // Not applicable
        executeTest(String.format("this%s is foobar", CARET), true)
    }

    fun testOuterCamelCase() {
        // Inner and outer selection should be the same for camelCase

        // Single item selection
        // Caret in the middle of the subword
        executeTest(String.format("this is %sf%soo%sBar", START, CARET, END), inner = false)
        executeTest(String.format("this is foo%sB%sar%s", START, CARET, END), inner = false)
        // Caret at the start of the subword
        executeTest(String.format("this is %s%sfoo%sBar", START, CARET, END), inner = false)
        executeTest(String.format("this is foo%s%sBar%s", START, CARET, END), inner = false)
        // Caret at the end of the subword
        executeTest(String.format("this is %sfo%so%sBar", START, CARET, END), inner = false)
        executeTest(String.format("this is foo%sBa%sr%s", START, CARET, END), inner = false)

        // Count selections
        // Caret in the middle of the subword
        executeTest(String.format("this is %sf%sooBar%s", START, CARET, END), inner = false, size = 2)
        executeTest(String.format("this is foo%sB%sar%s", START, CARET, END), inner = false, size = 2)

        // Caret at the start of the subword
        executeTest(String.format("this is %s%sfooBar%s", START, CARET, END), inner = false, size = 2)
        executeTest(String.format("this is foo%s%sBar%s", START, CARET, END), inner = false, size = 2)
        // Caret at the end of the subword
        executeTest(String.format("this is %sfo%soBar%s", START, CARET, END), inner = false, size = 2)
        executeTest(String.format("this is foo%sBa%sr%s", START, CARET, END), inner = false, size = 2)

        // Subword = Word
        executeTest(String.format("this is %sfooba%sr%s", START, CARET, END), inner = false)

        // Not applicable
        executeTest(String.format("this%s is foobar", CARET), true, false)
    }

    fun testInnerSnakeCase() {
        // Caret in the middle of the subword
        executeTest(String.format("this is %sf%soo%s_bar", START, CARET, END))
        executeTest(String.format("this is foo_%sb%sar%s", START, CARET, END))
        // Caret at the start of the subword
        executeTest(String.format("this is %s%sfoo%s_bar", START, CARET, END))
        executeTest(String.format("this is foo_%s%sbar%s", START, CARET, END))
        // Caret at the end of the subword
        executeTest(String.format("this is %sfoo%s%s_bar", START, END, CARET))
        executeTest(String.format("this is foo_%sba%sr%s", START, CARET, END))
    }

    fun testOuterSnakeCase() {
        // Single item selection
        // Caret in the middle of the subword
        executeTest(String.format("this is %sf%soo_%sbar", START, CARET, END), inner = false)
        executeTest(String.format("this is foo_%sb%sar%s", START, CARET, END), inner = false)
        // Caret at the start of the subword
        executeTest(String.format("this is %s%sfoo_%sbar", START, CARET, END), inner = false)
        executeTest(String.format("this is foo_%s%sbar%s", START, CARET, END), inner = false)
        // Caret at the end of the subword
        executeTest(String.format("this is %sfoo%s_%sbar", START, CARET, END), inner = false)
        executeTest(String.format("this is foo_%sba%sr%s", START, CARET, END), inner = false)
        
        // Count selections
        // Caret in the middle of the subword
        executeTest(String.format("this is %sf%soo_bar_%sco", START, CARET, END), inner = false, size = 2)
        executeTest(String.format("this is foo_%sb%sar_co%s", START, CARET, END), inner = false, size = 2)
        // Caret at the start of the subword
        executeTest(String.format("this is %s%sfoo_bar_%sco", START, CARET, END), inner = false, size = 2)
        executeTest(String.format("this is foo_%s%sbar_co%s", START, CARET, END), inner = false, size = 2)
        // Caret at the end of the subword
        executeTest(String.format("this is %sfoo%s_bar_%sco", START, CARET, END), inner = false, size = 2)
        executeTest(String.format("this is foo_%sba%sr_co%s", START, CARET, END), inner = false, size = 2)

    }

    fun testInnerDashCase() {
        // Caret in the middle of the subword
        executeTest(String.format("this is %sf%soo%s-bar", START, CARET, END))
        executeTest(String.format("this is foo-%sb%sar%s", START, CARET, END))
        // Caret at the start of the subword
        executeTest(String.format("this is %s%sfoo%s-bar", START, CARET, END))
        executeTest(String.format("this is foo-%s%sbar%s", START, CARET, END))
        // Caret at the end of the subword
        executeTest(String.format("this is %sfoo%s%s-bar", START, END, CARET))
        executeTest(String.format("this is foo-%sba%sr%s", START, CARET, END))
    }

    fun testOuterDashCase() {
        // Single item selection
        // Caret in the middle of the subword
        executeTest(String.format("this is %sf%soo-%sbar", START, CARET, END), inner = false)
        executeTest(String.format("this is foo-%sb%sar%s", START, CARET, END), inner = false)
        // Caret at the start of the subword
        executeTest(String.format("this is %s%sfoo-%sbar", START, CARET, END), inner = false)
        executeTest(String.format("this is foo-%s%sbar%s", START, CARET, END), inner = false)
        // Caret at the end of the subword
        executeTest(String.format("this is %sfoo%s-%sbar", START, CARET, END), inner = false)
        executeTest(String.format("this is foo-%sba%sr%s", START, CARET, END), inner = false)

        // Count selections
        // Caret in the middle of the subword
        executeTest(String.format("this is %sf%soo-bar-%sco", START, CARET, END), inner = false, size = 2)
        executeTest(String.format("this is foo-%sb%sar-co%s", START, CARET, END), inner = false,size=2)
        // Caret at the start of the subword
        executeTest(String.format("this is %s%sfoo-bar-%sco", START, CARET, END), inner = false,size=2)
        executeTest(String.format("this is foo-%s%sbar-co%s", START, CARET, END), inner = false,size=2)
        // Caret at the end of the subword
        executeTest(String.format("this is %sfoo%s-bar-%sco", START, CARET, END), inner = false,size=2)
        executeTest(String.format("this is foo-%sba%sr-co%s", START, CARET, END), inner = false,size=2)
    }
}
