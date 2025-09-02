package com.magidc.ideavim.anyObject

import com.magidc.ideavim.anyobject.handlers.AnyBlockCommentHandlers
import com.magidc.ideavim.anyobject.BaseTest

class AnyBlockCommentTest : BaseTest(AnyBlockCommentHandlers()) {

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
