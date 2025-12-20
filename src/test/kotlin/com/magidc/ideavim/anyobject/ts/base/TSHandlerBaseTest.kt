package com.magidc.ideavim.anyobject.ts.base

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.intellij.openapi.diagnostic.Logger
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.maddyhome.idea.vim.api.VimEditor
import com.magidc.ideavim.anyobject.handlers.base.TSBasedHandler
import org.apache.commons.lang3.StringUtils
import java.io.File
import java.io.InputStreamReader

abstract class TSHandlerBaseTest(val handler: TSBasedHandler) : BasePlatformTestCase() {
    companion object {
        val caretPositionRegex = Regex("<caret_(\\d+)>")
        private val LOG = Logger.getInstance(TSHandlerBaseTest::class.java)
    }

    private data class TestData(
        val filePath: String,
        val code: String,
        val inner: Map<String, String>,
        val around: Map<String, String>,
        val caretPositions: Map<String, Int>
    )


    private lateinit var testFilePaths: List<String>
    private val handlerName = handler.javaClass.simpleName.lowercase().removeSuffix("handler")

    private fun getTestData(testFilePath: String): TestData {
        val inputStream = javaClass.classLoader.getResourceAsStream(testFilePath) ?: throw IllegalArgumentException("Test data not found at $testFilePath")
        val json = Gson().fromJson(InputStreamReader(inputStream), JsonObject::class.java)
        val extension = json.get("extension").asString
        val inner = json.getAsJsonObject("inner").entrySet().associate { it.key to it.value.asString }
        val around = json.getAsJsonObject("around").entrySet().associate { it.key to it.value.asString }
        val testFilePath = "$handlerName.$extension"

        var rawCode = json.get("code").asString
        println("Test code for: $testFilePath")
        println(rawCode)
        println("--------------------------------------------------")
        val caretPositions: MutableMap<String, Int> = mutableMapOf()
        while (true) {
            val match = caretPositionRegex.find(rawCode) ?: break
            caretPositions[StringUtils.substringBetween(match.value, "_", ">")] = match.range.first
            rawCode = rawCode.removeRange(match.range)
        }
        val code = rawCode.replace(caretPositionRegex, "")
        return TestData(testFilePath, code, inner, around, caretPositions)
    }

    override fun setUp() {
        super.setUp()
        val basePath = "testData/$handlerName"
        val testDataDir = javaClass.classLoader.getResource(basePath) ?: throw IllegalArgumentException("Test data not found")
        TSBasedHandler.setDocumentCacheMaxSize(0)

        testFilePaths = File(testDataDir.path).listFiles()
            ?.filter { it.extension == "json" }
            ?.map { "$basePath/${it.name}" } ?: emptyList()
    }

    fun testInner() = testFilePaths.forEach { doTestHandler(getTestData(it), true) }
    fun testAround() = testFilePaths.forEach { doTestHandler(getTestData(it), false) }

    private fun doTestHandler(testData: TestData, inner: Boolean) {
        val assertions = if (inner) testData.inner else testData.around
        val editor = MockVimEditor(testData.code, testData.filePath, 0)

        for (caretPositionEntry in testData.caretPositions.entries) {
            val caretKey = caretPositionEntry.key
            val message = "Testing \"${testData.filePath}\" ${if (inner) "inner" else "around"} selection at caret offset $caretKey"
            LOG.info(message)
            editor.currentCaretOffset = caretPositionEntry.value

            val expected = assertions[caretKey] ?: continue
            val actual = executeHandlerSelection(editor, inner)
            assertEquals(message, expected, actual)
        }
    }

    private fun executeHandlerSelection(editor: VimEditor, inner: Boolean): String {
        val textRange = handler.findSelection(editor, inner, 1) ?: return ""
        return editor.text().substring(textRange.startOffset, textRange.endOffset)
    }
}