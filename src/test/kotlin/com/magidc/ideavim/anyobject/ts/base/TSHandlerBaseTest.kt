package com.magidc.ideavim.anyobject.ts.base

import com.google.common.collect.HashMultimap
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.maddyhome.idea.vim.api.VimEditor
import com.magidc.ideavim.anyobject.handlers.base.TSBasedHandler
import org.apache.commons.lang3.StringUtils
import org.junit.Assert
import java.io.File
import java.io.InputStreamReader

abstract class TSHandlerBaseTest(val handler: TSBasedHandler) : BasePlatformTestCase() {
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

    private lateinit var testFilePaths: List<String>
    private val handlerName = handler.javaClass.simpleName.lowercase().removeSuffix("handler")
    private val testFailResults = HashMultimap.create<TestData, TestFailResult>()

    private fun getTestData(testFilePath: String): TestData {
        val inputStream = javaClass.classLoader.getResourceAsStream(testFilePath) ?: throw IllegalArgumentException("Test data not found at $testFilePath")
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
        val testDataDir = javaClass.classLoader.getResource(basePath) ?: throw IllegalArgumentException("Test data not found")
        TSBasedHandler.setDocumentCacheMaxSize(0)

        testFilePaths = File(testDataDir.path).listFiles()
            ?.filter { it.extension == "json" }
            ?.map { "$basePath/${it.name}" }
            ?.sorted() ?: emptyList()

    }

    fun testHandler() {
        testFilePaths.forEach { doTestHandler(getTestData(it), true) }
        testFilePaths.forEach { doTestHandler(getTestData(it), false) }
        if (!testFailResults.isEmpty) {
            for (entry in testFailResults.asMap()) {
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

    private fun doTestHandler(testData: TestData, inner: Boolean) {
        val assertions = if (inner) testData.inner else testData.around
        val editor = MockVimEditor(testData.code, testData.filePath, 0)

        for (caretPositionEntry in testData.caretPositions.entries) {
            editor.currentCaretOffset = caretPositionEntry.value
            val expected = assertions[caretPositionEntry.key] ?: continue
            val actual = executeHandlerSelection(editor, inner)
            if (actual != expected)
                testFailResults.put(testData, TestFailResult(inner, caretPositionEntry.key, caretPositionEntry.value, actual, expected))
        }
    }

    private fun executeHandlerSelection(editor: VimEditor, inner: Boolean): String {
        val textRange = handler.findSelection(editor, inner, 1) ?: return ""
        return editor.text().substring(textRange.startOffset, textRange.endOffset)
    }
}