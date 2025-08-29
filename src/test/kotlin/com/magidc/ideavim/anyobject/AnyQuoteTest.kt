package com.magidc.ideavim.anyObject

import com.magidc.ideavim.anyObject.handlers.AnyQuoteHandlers

class AnyQuoteTest : BaseTest(AnyQuoteHandlers()) {

    fun testAnyQuoteCaretInside() {
        testInner(String.format("this is a '%s te%sst %s'", START, CARET, END))
        testOuter(String.format("this is a %s' te%sst '%s", START, CARET, END))
    }

    fun testAnyQuoteCaretOnRightDelimiter() {
        testInner(String.format("this is a '%s test %s%s'", START, END, CARET))
        testOuter(String.format("this is a %s' test %s'%s", START, CARET, END))
    }

    fun testAnyQuoteCaretOnLeftDelimiter() {
        testInner(String.format("this is a %s'%s test %s'", CARET, START, END))
        testOuter(String.format("this is a %s%s' test '%s", START, CARET, END))
    }

    fun testAnyQuoteNested() {
        testInner(String.format("this is 'a '%s te%sst %s' with nested 'quotes", START, CARET, END))
        testOuter(String.format("this is 'a %s' te%sst '%s with nested 'quotes", START, CARET, END))

        // Shorter selections should be prioritized
        // Single quote selection is shorter
        testInner(String.format("this is'%s \"a te%sst %s' with nested quotes\"", START, CARET, END))
        // Double quote selection is shorter
        testInner(String.format("'this is a larger \"%ste%sst' with%s\" nested quotes", START, CARET, END))
    }
}
