package com.magidc.ideavim.anyobject.ts.base

import com.maddyhome.idea.vim.api.BufferPosition
import com.maddyhome.idea.vim.api.CaretRegisterStorage
import com.maddyhome.idea.vim.api.ExecutionContext
import com.maddyhome.idea.vim.api.ImmutableVimCaret
import com.maddyhome.idea.vim.api.LineDeleteShift
import com.maddyhome.idea.vim.api.LocalMarkStorage
import com.maddyhome.idea.vim.api.SelectionInfo
import com.maddyhome.idea.vim.api.VimCaret
import com.maddyhome.idea.vim.api.VimCaretListener
import com.maddyhome.idea.vim.api.VimDocument
import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.api.VimFoldRegion
import com.maddyhome.idea.vim.api.VimIndentConfig
import com.maddyhome.idea.vim.api.VimScrollingModel
import com.maddyhome.idea.vim.api.VimVirtualFile
import com.maddyhome.idea.vim.api.VimVisualPosition
import com.maddyhome.idea.vim.common.ChangesListener
import com.maddyhome.idea.vim.common.LiveRange
import com.maddyhome.idea.vim.common.TextRange
import com.maddyhome.idea.vim.common.VimEditorReplaceMask
import com.maddyhome.idea.vim.group.visual.VisualChange
import com.maddyhome.idea.vim.state.mode.Mode
import com.maddyhome.idea.vim.state.mode.SelectionType

@Suppress("unused")
class MockVimEditor(val text: String, val filePath: String, var currentCaretOffset: Int, override var replaceMask: VimEditorReplaceMask? = null) : VimEditor {
    override val document: VimDocument get() = MockVimDocument()
    override fun text(): CharSequence = text
    override fun getVirtualFile(): VimVirtualFile = MockVirtualFile(filePath, "", null)
    override fun currentCaret(): VimCaret = MockCaret(currentCaretOffset, "")
    override fun toString(): String = getVirtualFile().path

    override var mode: Mode get() = TODO("Mock"); set(value) {}
    override var isReplaceCharacter: Boolean get() = TODO("Mock"); set(value) {}
    override val lfMakesNewLine: Boolean get() = TODO("Mock")
    override var vimChangeActionSwitchMode: Mode? get() = TODO("Mock"); set(value) {}
    override val indentConfig: VimIndentConfig get() = TODO("Mock")
    override val projectId: String get() = TODO("Mock")
    override var vimLastSelectionType: SelectionType? get() = TODO("Mock"); set(value) {}
    override var insertMode: Boolean get() = TODO("Mock"); set(value) {}
    override fun fileSize(): Long = TODO("Mock")
    override fun nativeLineCount(): Int = TODO("Mock")
    override fun getLineRange(line: Int): Pair<Int, Int> = TODO("Mock")
    override fun carets(): List<VimCaret> = TODO("Mock")
    override fun nativeCarets(): List<VimCaret> = TODO("Mock")
    override fun forEachCaret(action: (VimCaret) -> Unit) = TODO("Mock")
    override fun forEachNativeCaret(action: (VimCaret) -> Unit, reverse: Boolean) = TODO("Mock")
    override fun isInForEachCaretScope(): Boolean = TODO("Mock")
    override fun primaryCaret(): VimCaret = TODO("Mock")
    override fun isWritable(): Boolean = TODO("Mock")
    override fun isDocumentWritable(): Boolean = TODO("Mock")
    override fun isOneLineMode(): Boolean = TODO("Mock")
    override fun search(pair: Pair<Int, Int>, editor: VimEditor, shiftType: LineDeleteShift): Pair<Pair<Int, Int>, LineDeleteShift> = TODO("Mock")
    override fun offsetToBufferPosition(offset: Int): BufferPosition = TODO("Mock")
    override fun bufferPositionToOffset(position: BufferPosition): Int = TODO("Mock")
    override fun offsetToVisualPosition(offset: Int): VimVisualPosition = TODO("Mock")
    override fun visualPositionToOffset(position: VimVisualPosition): Int = TODO("Mock")
    override fun visualPositionToBufferPosition(position: VimVisualPosition): BufferPosition = TODO("Mock")
    override fun bufferPositionToVisualPosition(position: BufferPosition): VimVisualPosition = TODO("Mock")
    override fun deleteString(range: TextRange) = TODO("Mock")
    override fun getScrollingModel(): VimScrollingModel = TODO("Mock")
    override fun removeCaret(caret: VimCaret) = TODO("Mock")
    override fun addCaret(offset: Int): VimCaret = TODO("Mock")
    override fun removeSecondaryCarets() = TODO("Mock")
    override fun vimSetSystemBlockSelectionSilently(start: BufferPosition, end: BufferPosition) = TODO("Mock")
    override fun getLineStartOffset(line: Int): Int = TODO("Mock")
    override fun getLineEndOffset(line: Int): Int = TODO("Mock")
    override fun addCaretListener(listener: VimCaretListener) = TODO("Mock")
    override fun removeCaretListener(listener: VimCaretListener) = TODO("Mock")
    override fun isDisposed(): Boolean = TODO("Mock")
    override fun removeSelection() = TODO("Mock")
    override fun getPath(): String = TODO("Mock")
    override fun extractProtocol(): String = TODO("Mock")
    override fun exitInsertMode(context: ExecutionContext) = TODO("Mock")
    override fun exitSelectModeNative(adjustCaret: Boolean) = TODO("Mock")
    override fun isTemplateActive(): Boolean = TODO("Mock")
    override fun startGuardedBlockChecking() = TODO("Mock")
    override fun stopGuardedBlockChecking() = TODO("Mock")
    override fun hasUnsavedChanges(): Boolean = TODO("Mock")
    override fun getLastVisualLineColumnNumber(line: Int): Int = TODO("Mock")
    override fun createLiveMarker(start: Int, end: Int): LiveRange = TODO("Mock")
    override fun createIndentBySize(size: Int): String = TODO("Mock")
    override fun getFoldRegionAtOffset(offset: Int): VimFoldRegion = TODO("Mock")
    override fun <T : ImmutableVimCaret> findLastVersionOfCaret(caret: T): T = TODO("Mock")
}

class MockVimDocument : VimDocument {
    override fun addChangeListener(listener: ChangesListener) {}
    override fun removeChangeListener(listener: ChangesListener) {}
    override fun getOffsetGuard(offset: Int): LiveRange = TODO("Mock")
}

class MockVirtualFile(override val path: String, override val protocol: String, override val extension: String?) : VimVirtualFile

@Suppress("unused")
class MockCaret(override val offset: Int, override val id: String) : VimCaret {
    override fun moveToBufferPosition(position: BufferPosition) = TODO("Mock")
    override fun moveToInlayAwareOffset(newOffset: Int): VimCaret = TODO("Mock")
    override fun moveToOffsetNative(offset: Int) = TODO("Mock")
    override fun moveToVisualPosition(position: VimVisualPosition) = TODO("Mock")
    override fun removeSelection() = TODO("Mock")
    override fun resetLastColumn() = TODO("Mock")
    override fun setSelection(start: Int, end: Int) = TODO("Mock")
    override fun setVimLastColumnAndGetCaret(col: Int): VimCaret = TODO("Mock")
    override fun vimSelectionStartClear() = TODO("Mock")
    override var vimInsertStart: LiveRange get() = TODO("Mock"); set(value) {}
    override var vimLastColumn: Int get() = TODO("Mock"); set(value) {}
    override var vimLastVisualOperatorRange: VisualChange? get() = TODO("Mock"); set(value) {}
    override var vimSelectionStart: Int get() = TODO("Mock"); set(value) {}
    override fun getBufferPosition(): BufferPosition = TODO("Mock")
    override fun getLine(): Int = TODO("Mock")
    override fun getVisualPosition(): VimVisualPosition = TODO("Mock")
    override fun hasSelection(): Boolean = TODO("Mock")
    override val editor: VimEditor get() = TODO("Mock")
    override val isPrimary: Boolean get() = TODO("Mock")
    override val isValid: Boolean get() = TODO("Mock")
    override var lastSelectionInfo: SelectionInfo get() = TODO("Mock"); set(value) {}
    override val markStorage: LocalMarkStorage get() = TODO("Mock")
    override val registerStorage: CaretRegisterStorage get() = TODO("Mock")
    override val selectionEnd: Int get() = TODO("Mock")
    override val selectionStart: Int get() = TODO("Mock")
    override val vimLine: Int get() = TODO("Mock")
    override val visualLineStart: Int get() = TODO("Mock")
}