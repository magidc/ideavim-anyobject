package com.magidc.ideavim.anyobject.ts

import com.magidc.ideavim.anyobject.handlers.AnyStringHandler
import com.magidc.ideavim.anyobject.ts.base.TSHandlerBaseTest

class AnyStringTest : TSHandlerBaseTest(AnyStringHandler()) {
    override fun testCpp() = doTestLanguageHandler("cpp.json")
    override fun testCsharp() = doTestLanguageHandler("csharp.json")
    override fun testGo() = doTestLanguageHandler("go.json")
    override fun testJava() = doTestLanguageHandler("java.json")
    override fun testJavascript() = doTestLanguageHandler("javascript.json")
    override fun testKotlin() = doTestLanguageHandler("kotlin.json")
    override fun testPhp() = doTestLanguageHandler("php.json")
    override fun testPython() = doTestLanguageHandler("python.json")
    override fun testR() = doTestLanguageHandler("r.json")
    override fun testRuby() = doTestLanguageHandler("ruby.json")
    override fun testRust() = doTestLanguageHandler("rust.json")
    override fun testScala() = doTestLanguageHandler("scala.json")
    override fun testSwift() = doTestLanguageHandler("swift.json")
    override fun testTypescript() = doTestLanguageHandler("typescript.json")
    override fun testJson() = doTestLanguageHandler("json.json")
    override fun testYaml() = doTestLanguageHandler("yaml.json")
}
