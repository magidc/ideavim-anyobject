package com.magidc.ideavim.anyobject.text

import com.magidc.ideavim.anyobject.handlers.AnyBracketHandler

class AnyBracketTest : TextHandlerBaseTest(AnyBracketHandler()) {
    fun testAnyBracketCaretBefore() {
        testInner(String.format("this is%s a {%s test %s}", CARET, START, END))
        testOuter(String.format("this is%s a %s{ test }%s", CARET, START, END))

        testInner(String.format("this is a %s{%s test %s}", CARET, START, END))
        testOuter(String.format("this is a %s%s{ test }%s", CARET, START, END))
    }

    fun testAnyBracketCaretInside() {
        testInner(String.format("this is a {%s te%sst %s}", START, CARET, END))
        testOuter(String.format("this is a %s{ te%sst }%s", START, CARET, END))
    }

    fun testAnyBracketCaretOnRightDelimiter() {
        testInner(String.format("this is a {%s test %s%s}", START, END, CARET))
        testOuter(String.format("this is a %s{ test %s}%s", START, CARET, END))
    }

    fun testAnyBracketCaretOnLeftDelimiter() {
        testInner(String.format("this is a %s[%s test %s]", CARET, START, END))
        testOuter(String.format("this is a %s%s[ test ]%s", START, CARET, END))
    }

    fun testAnyBracketNested() {
        testInner(String.format("this is [a {%s te%sst %s} with nested ]Brackets", START, CARET, END))
        testOuter(String.format("this is [a %s{ te%sst }%s with nested ]Brackets", START, CARET, END))

        testInner(String.format("this is [a {%s test %s%s} with nested ]Brackets", START, END, CARET))
        testOuter(String.format("this is [a %s{ test %s}%s with nested ]Brackets", START, CARET, END))

        testInner(String.format("this [%sis %sa { test } with nested %s]Brackets", START, CARET, END))
        testOuter(String.format("this %s[is %sa { test } with nested ]%sBrackets", START, CARET, END))
    }

    fun testAnyMultiCharBracketCaretInside() {
        testInner(String.format("this is a <%s te%sst %s/> ", START, CARET, END))
        testOuter(String.format("this is a %s< te%sst />%s ", START, CARET, END))
    }

    fun testAnyMulticharBracketCaretOnRightDelimiter() {
        testInner(String.format("this is a <%s test %s%s/>", START, END, CARET))
        testOuter(String.format("this is a %s< test %s/>%s", START, CARET, END))
    }

    fun testAnyMultiCharBracketCaretOnLeftDelimiter() {
        testInner(String.format("this is a %s<%s test %s/>", CARET, START, END))
        testOuter(String.format("this is a %s%s< test />%s", START, CARET, END))
    }

    fun testAnyMultiCharBracketsNested() {
        testInner(String.format("this is [a <%s te%sst %s/> with nested ]Brackets", START, CARET, END))
        testOuter(String.format("this is [a %s< te%sst />%s with nested ]Brackets", START, CARET, END))

        testInner(String.format("this [%sis %sa < test /> with nested %s]Brackets", START, CARET, END))
        testOuter(String.format("this %s[is %sa < test /> with nested ]%sBrackets", START, CARET, END))
    }
}
