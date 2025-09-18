package com.magidc.ideavim.anyobject

import com.magidc.ideavim.anyobject.handlers.AnyBlockCommentHandlers

class AnyBlockCommentTest : TextHandlerBaseTest(AnyBlockCommentHandlers()) {

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
