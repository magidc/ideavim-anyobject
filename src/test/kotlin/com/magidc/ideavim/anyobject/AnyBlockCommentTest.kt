package com.magidc.ideavim.anyobject

import com.magidc.ideavim.anyobject.handlers.AnyBlockCommentHandler

class AnyBlockCommentTest : TextHandlerBaseTest(AnyBlockCommentHandler()) {

    fun testJavadocBlockCommentCaretBefore() {
        testInner(String.format("this is%s a /**%s test %s*/", CARET, START, END))
        testOuter(String.format("this is%s a %s/** test */%s", CARET, START, END))
    }

    fun testJavadocBlockComment() {
        testInner(String.format("this is a /**%s te%sst %s*/", START, CARET, END))
        testOuter(String.format("this is a %s/** te%sst */%s", START, CARET, END))
    }

    fun testJavaBlockComment() {
        testInner(String.format("this is a /*%s te%sst %s*/", START, CARET, END))
        testOuter(String.format("this is a %s/* te%sst */%s", START, CARET, END))
    }

    fun testPythonBlockComment() {
        testInner(String.format("this is a \"\"\"%s te%sst %s\"\"\"", START, CARET, END))
        testOuter(String.format("this is a %s\"\"\" te%sst \"\"\"%s", START, CARET, END))

        testInner(String.format("this is a '''%s te%sst %s'''", START, CARET, END))
        testOuter(String.format("this is a %s''' te%sst '''%s", START, CARET, END))
    }

    fun testHTMLBlockComment() {
        testInner(String.format("this is a <!--%s te%sst %s-->", START, CARET, END))
        testOuter(String.format("this is a %s<!-- te%sst -->%s", START, CARET, END))
    }
}
