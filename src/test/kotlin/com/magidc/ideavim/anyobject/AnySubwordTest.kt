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
        // Single item selection
        // Caret in the middle of the subword
        executeTest(String.format("this is %sf%soo%sBar", START, CARET, END))
        executeTest(String.format("this is foo%sB%sar%s", START, CARET, END))
        // Caret at the start of the subword
        executeTest(String.format("this is %s%sfoo%sBar", START, CARET, END))
        executeTest(String.format("this is foo%s%sBar%s", START, CARET, END))
        // Caret at the end of the subword
        executeTest(String.format("this is %sfo%so%sBar", START, CARET, END))
        executeTest(String.format("this is foo%sBa%sr%s", START, CARET, END))

        // Count selections
        // Caret in the middle of the subword
        executeTest(String.format("this is %sf%sooBar%sCo other", START, CARET, END), size = 2)
        executeTest(String.format("this is foo%sB%sar%s other", START, CARET, END), size = 2)
        // Caret at the start of the subword
        executeTest(String.format("this is %s%sfooBar%sCo other", START, CARET, END), size = 2)
        executeTest(String.format("this is foo%s%sBar%s other", START, CARET, END), size = 2)
        // Caret at the end of the subword
        executeTest(String.format("this is %sfo%soBar%sCo other", START, CARET, END), size = 2)
        executeTest(String.format("this is foo%sBa%sr%s other", START, CARET, END), size = 2)

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
        executeTest(String.format("this is %sf%sooBar%sCo other", START, CARET, END), inner = false, size = 2)
        executeTest(String.format("this is foo%sB%sar%s other", START, CARET, END), inner = false, size = 2)

        // Caret at the start of the subword
        executeTest(String.format("this is %s%sfooBar%sCo other", START, CARET, END), inner = false, size = 2)
        executeTest(String.format("this is foo%s%sBar%s other", START, CARET, END), inner = false, size = 2)
        // Caret at the end of the subword
        executeTest(String.format("this is %sfo%soBar%sCo other", START, CARET, END), inner = false, size = 2)
        executeTest(String.format("this is foo%sBa%sr%s other", START, CARET, END), inner = false, size = 2)

        // Subword = Word
        executeTest(String.format("this is %sfooba%sr%s", START, CARET, END), inner = false)

        // Not applicable
        executeTest(String.format("this%s is foobar", CARET), true, false)
    }

    fun testInnerSnakeCase() {
        // Single item selection
        // Caret in the middle of the subword
        executeTest(String.format("this is %sf%soo%s_bar", START, CARET, END))
        executeTest(String.format("this is foo_%sb%sar%s", START, CARET, END))
        // Caret at the start of the subword
        executeTest(String.format("this is %s%sfoo%s_bar", START, CARET, END))
        executeTest(String.format("this is foo_%s%sbar%s", START, CARET, END))
        // Caret at the end of the subword
        executeTest(String.format("this is %sfoo%s%s_bar", START, END, CARET))
        executeTest(String.format("this is foo_%sba%sr%s", START, CARET, END))

        // Count selections
        // Caret in the middle of the subword
        executeTest(String.format("this is %sf%soo_bar%s_co other", START, CARET, END), size = 2)
        executeTest(String.format("this is foo_%sb%sar%s other", START, CARET, END), size = 2)
        // Caret at the start of the subword
        executeTest(String.format("this is %s%sfoo_bar%s_co other", START, CARET, END), size = 2)
        executeTest(String.format("this is foo_%s%sbar%s other", START, CARET, END), size = 2)
        // Caret at the end of the subword
        executeTest(String.format("this is %sfoo%s_bar%s_co other", START, CARET, END), size = 2)
        executeTest(String.format("this is foo_%sba%sr%s other", START, CARET, END), size = 2)
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
        executeTest(String.format("this is %sf%soo_bar_%sco other", START, CARET, END), inner = false, size = 2)
        executeTest(String.format("this is foo_%sb%sar%s other", START, CARET, END), inner = false, size = 2)
        // Caret at the start of the subword
        executeTest(String.format("this is %s%sfoo_bar_%sco other", START, CARET, END), inner = false, size = 2)
        executeTest(String.format("this is foo_%s%sbar%s other", START, CARET, END), inner = false, size = 2)
        // Caret at the end of the subword
        executeTest(String.format("this is %sfoo%s_bar_%sco other", START, CARET, END), inner = false, size = 2)
        executeTest(String.format("this is foo_%sba%sr%s other", START, CARET, END), inner = false, size = 2)
    }

    fun testInnerDashCase() {
        executeTest(String.format("this is %sf%soo-bar%s-co other", START, CARET, END), size = 2)

        // Single item selection
        // Caret in the middle of the subword
        executeTest(String.format("this is %sf%soo%s-bar", START, CARET, END))
        executeTest(String.format("this is foo-%sb%sar%s", START, CARET, END))
        // Caret at the start of the subword
        executeTest(String.format("this is %s%sfoo%s-bar", START, CARET, END))
        executeTest(String.format("this is foo-%s%sbar%s", START, CARET, END))
        // Caret at the end of the subword
        executeTest(String.format("this is %sfoo%s%s-bar", START, END, CARET))
        executeTest(String.format("this is foo-%sba%sr%s", START, CARET, END))

        // Count selections
        // Caret in the middle of the subword
        executeTest(String.format("this is %sf%soo-bar%s-co other", START, CARET, END), size = 2)
        executeTest(String.format("this is foo-%sb%sar%s other", START, CARET, END), size = 2)
        // Caret at the start of the subword
        executeTest(String.format("this is %s%sfoo-bar%s-co other", START, CARET, END), size = 2)
        executeTest(String.format("this is foo-%s%sbar%s other", START, CARET, END), size = 2)
        // Caret at the end of the subword
        executeTest(String.format("this is %sfoo%s-bar%s-co other", START, CARET, END), size = 2)
        executeTest(String.format("this is foo-%sba%sr%s other", START, CARET, END), size = 2)
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
        executeTest(String.format("this is %sf%soo-bar-%sco other", START, CARET, END), inner = false, size = 2)
        executeTest(String.format("this is foo-%sb%sar%s other", START, CARET, END), inner = false, size = 2)
        // Caret at the start of the subword
        executeTest(String.format("this is %s%sfoo-bar-%sco other", START, CARET, END), inner = false, size = 2)
        executeTest(String.format("this is foo-%s%sbar%s other", START, CARET, END), inner = false, size = 2)
        // Caret at the end of the subword
        executeTest(String.format("this is %sfoo%s-bar-%sco other", START, CARET, END), inner = false, size = 2)
        executeTest(String.format("this is foo-%sba%sr%s other", START, CARET, END), inner = false, size = 2)
    }
}
