package com.magidc.ideavim.anyobject.ts.base

import com.google.common.collect.HashMultimap
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.magidc.ideavim.anyobject.handlers.base.TSBasedHandler
import org.apache.commons.lang3.StringUtils
import org.junit.Assert
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.net.URI
import java.net.URL

abstract class TSHandlerBaseTest(handler: TSBasedHandler) : BasePlatformTestCase() {
    companion object {
        val caretPositionRegex = Regex("<caret_(\\d+)>")
    }

    private data class TestData(
        val filePath: String,
        val code: String,
        val codeWithCarets: String,
        val inner: Map<String, String>,
        val around: Map<String, String>,
        val caretPositions: Map<String, Int>
    )

    private data class TestFailResult(
        val inner: Boolean,
        val caretId: String,
        val caretPosition: Int,
        val actual: String,
        val expected: String
    )

    private class TestFailResults {
        private val results = HashMultimap.create<TestData, TestFailResult>()

        fun add(testData: TestData, inner: Boolean, caretId: String, caretPosition: Int, actual: String, expected: String) {
            results.put(testData, TestFailResult(inner, caretId, caretPosition, actual, expected))
        }

        fun assertResults() {
            if (!results.isEmpty) {
                for (entry in results.asMap()) {
                    val testData = entry.key
                    println("Failed on test data: ${testData.filePath}")
                    println("Code:\n${testData.code}")
                    println("---------------------------------------")
                    println("Code with carets:\n${testData.codeWithCarets}")
                    println("---------------------------------------")
                    entry.value.map {
                        "${if (it.inner) "Inner" else "Around"} selection at caret ${it.caretId} [${it.caretPosition}].\n" +
                                "Expected:\n${it.expected}\n" +
                                "Actual:\n${it.actual}"
                    }.forEach { println(it) }
                    println("---------------------------------------")
                }
                Assert.fail()
            }
        }
    }

    val handlerWrapper: TestTSHandlerWrapper = TestTSHandlerWrapper(handler)
    val handlerName: String = handler.javaClass.simpleName.lowercase().removeSuffix("handler")
    private lateinit var testDataDir: URL

    private fun getTestData(testFileURI: URI): TestData {
        val inputStream = FileInputStream(File(testFileURI))
        val json = Gson().fromJson(InputStreamReader(inputStream), JsonObject::class.java)
        val extension = json.get("extension").asString
        val inner = json.getAsJsonObject("inner").entrySet().associate { it.key to it.value.asString }
        val around = json.getAsJsonObject("around").entrySet().associate { it.key to it.value.asString }
        val testFilePath = "$handlerName.$extension"
        val rawCode = json.get("code").asString
        var code = rawCode
        val caretPositions: MutableMap<String, Int> = mutableMapOf()
        while (true) {
            val match = caretPositionRegex.find(code) ?: break
            caretPositions[StringUtils.substringBetween(match.value, "_", ">")] = match.range.first
            code = code.removeRange(match.range)
        }
        return TestData(testFilePath, code, rawCode, inner, around, caretPositions)
    }

    override fun setUp() {
        super.setUp()
        val basePath = "testData/$handlerName"
        testDataDir = javaClass.classLoader.getResource(basePath) ?: throw IllegalArgumentException("Test data not found")
        TSBasedHandler.documentCache.maxSize = 0
    }

    fun doTestLanguageHandler(fileSuffix: String = ".json") {
        val testFileNames = File(testDataDir.path).listFiles { it.name.contains(fileSuffix) }
            ?.map { it.name }
            ?.sorted() ?: return

        val testFailResults = TestFailResults()
        testFileNames.forEach {
            val testFileURI = testDataDir.toURI().resolve(it)
            if (!File(testFileURI).exists()) return
            val testData = getTestData(testFileURI)
            doTestHandler(testData, false, testFailResults)
            doTestHandler(testData, true, testFailResults)
        }
        testFailResults.assertResults()
    }

    private fun doTestHandler(testData: TestData, inner: Boolean, testFailResults: TestFailResults) {
        val assertions = if (inner) testData.inner else testData.around
        val editor = MockVimEditor(testData.code, testData.filePath, 0)

        for (caretPositionEntry in testData.caretPositions.entries) {
            editor.currentCaretOffset = caretPositionEntry.value
            val expected = assertions[caretPositionEntry.key]?.trim() ?: continue
            val actual = handlerWrapper.findSelection(editor, inner, 1)?.let { editor.text().substring(it.startOffset, it.endOffset) }?.trim() ?: ""
            if (actual != expected)
                testFailResults.add(testData, inner, caretPositionEntry.key, caretPositionEntry.value, actual, expected)
        }
    }

    fun testAll() {
        doTestLanguageHandler()
        println("Sorted target types: ${handlerWrapper.sortedTargetTypes.joinToString(", ") { "\"$it\"" }}")
    }

    fun testCpp() = doTestLanguageHandler("cpp.json")
    fun testCsharp() = doTestLanguageHandler("csharp.json")
    fun testGo() = doTestLanguageHandler("go.json")
    fun testJava() = doTestLanguageHandler("java.json")
    fun testJavascript() = doTestLanguageHandler("javascript.json")
    fun testKotlin() = doTestLanguageHandler("kotlin.json")
    fun testObjectivec() = doTestLanguageHandler("objectivec.json")
    fun testPhp() = doTestLanguageHandler("php.json")
    fun testPython() = doTestLanguageHandler("python.json")
    fun testR() = doTestLanguageHandler("r.json")
    fun testRuby() = doTestLanguageHandler("ruby.json")
    fun testRust() = doTestLanguageHandler("rust.json")
    fun testScala() = doTestLanguageHandler("scala.json")
    fun testSwift() = doTestLanguageHandler("swift.json")
    fun testTypescript() = doTestLanguageHandler("typescript.json")
}