package com.magidc.ideavim.anyobject.handlers

import com.intellij.psi.PsiElement
import com.intellij.psi.util.childLeafs
import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.common.TextRange
import com.magidc.ideavim.anyobject.handlers.base.AbstractPSIBasedHandler
import com.magidc.ideavim.anyobject.handlers.base.getCareOffset


open class AnyItemHandler : AbstractPSIBasedHandler() {
    companion object {
        val openDelimiters = setOf("(", "{", "[")
        val closeDelimiters = setOf(")", "}", "]")
        private fun <T> List<T>.getLoopNext(index: Int): T = if (index < size - 1) get(index + 1) else first()
        private fun <T> List<T>.getLoopPrevious(index: Int): T = if (index > 0) get(index - 1) else last()
    }

    protected fun isDelimiter(element: PsiElement): Boolean {
        val text = element.text
        return "," == text || openDelimiters.contains(text) || closeDelimiters.contains(text)
    }

    override fun allowsCountSelection(): Boolean = true

    override fun cleanPrefix(text: String, language: String): String {
        if (language == "XML") return text.replace("XML", "")
        if (language == "YAML") return text.replace("YAML", "")
        if (language == "JSON") return text.replace("JSON", "")
        return super.cleanPrefix(text, language)
    }

    override fun acceptElement(element: PsiElement, language: String, acceptedNormalizedTypes: Set<String>): Boolean {
        if (element.text.isBlank() || isDelimiter(element)) return false
        val parentElementTypeName = element.parent?.toElementTypeName() ?: return false
        return (parentElementTypeName.contains("ARRAY") || parentElementTypeName.contains("LIST_") || parentElementTypeName.contains("TUPLE_"))
    }

    private class ItemRange(val startOuterOffset: Int, val endOuterOffset: Int, val startInnerOffset: Int, val endInnerOffset: Int, val index: Int, val text: String) {
        override fun toString(): String = text
        fun containsOffset(offset: Int): Boolean = offset in startOuterOffset until endOuterOffset + 1
    }

    /**
     * Indentifies the main component ranges within the given element
     */
    private fun findItemRanges(element: PsiElement, fromCaretOffset: Int = -1, maxSize: Int = Int.MAX_VALUE): List<ItemRange> {
        if (maxSize == 0) return emptyList()

        val childLeafs = element.childLeafs()

        if (!childLeafs.none() && childLeafs.first().text == "(" && childLeafs.last().text == ")") {
            val itemStack = java.util.ArrayDeque<String>()
            val ranges = mutableListOf<ItemRange>()
            val itemTextBuilder = StringBuilder()
            var fromInner = childLeafs.first().textRange.endOffset
            var fromOuter = fromInner
            var isValid = fromCaretOffset == -1
            var i = 0

            for (leaf in childLeafs) {
                val leafText = leaf.text
                if (openDelimiters.contains(leafText))
                    itemStack.push(leafText)
                else if (closeDelimiters.contains(leafText)) {
                    // Stack should not be empty in well-formed code. There must be at least the main item level
                    if (itemStack.isEmpty()) return emptyList()
                    itemStack.pop()
                } else {
                    if (leafText == "," && itemStack.size == 1) {
                        val itemRange = ItemRange(
                            fromOuter,
                            leaf.textRange.endOffset,
                            fromInner,
                            leaf.textRange.startOffset,
                            i++,
                            itemTextBuilder.toString()
                        )

                        itemTextBuilder.clear()
                        fromInner = leaf.textRange.endOffset
                        fromOuter = leaf.textRange.startOffset
                        isValid = isValid || itemRange.containsOffset(fromCaretOffset)
                        if (isValid) {
                            ranges.add(itemRange)
                            if (ranges.size == maxSize) return ranges
                        }
                    } else {
                        if (leafText.isBlank() && itemTextBuilder.isEmpty())
                        // Shrink inner range to exclude trailing whitespaces
                            fromInner += leaf.textLength
                        else
                            itemTextBuilder.append(leafText)
                    }
                }
            }
            val itemRange = ItemRange(
                fromOuter,
                childLeafs.last().textRange.startOffset,
                fromInner,
                childLeafs.last().textRange.startOffset,
                i,
                itemTextBuilder.toString()
            )
            isValid = isValid || itemRange.containsOffset(fromCaretOffset)
            if (isValid)
                ranges.add(itemRange)
            return ranges
        }
        return emptyList()
    }


    override fun findSelection(editor: VimEditor, isInner: Boolean, size: Int): TextRange? {
        val currentElement = findCurrentElement(editor) ?: return null
        val objectElement = findObjectElement(currentElement) ?: super.getNextElement(currentElement, false) ?: return null

        if (objectElement.text.isBlank() || size == 0) return null
        val ranges = findItemRanges(objectElement.parent, editor.getCareOffset(), size)
        if (ranges.isEmpty()) return null
        if (isInner)
            return TextRange(ranges.first().startInnerOffset, ranges.last().endInnerOffset)
        // Outer selection of first items includes separator AFTER the items. For other items, it includes separator BEFORE the items.
        // This approach is more conformable for delete and change motions as it avoids leaving trailing commas
        val first = ranges.first()
        if (first.index == 0)
            return TextRange(first.startInnerOffset, ranges.last().endOuterOffset)
        return TextRange(first.startOuterOffset, ranges.last().endInnerOffset)
    }

    /**
     * For jumps, items iterated in loop
     */
    override fun findJumpElementStartOffset(editor: VimEditor, next: Boolean): Int? {
        val currentElement = findCurrentElement(editor) ?: return null
        val objectElement = findObjectElement(currentElement) ?: return super.findJumpElementStartOffset(editor, next)
        val ranges = findItemRanges(objectElement.parent)
        val idx = ranges.indexOfFirst { it.containsOffset(editor.getCareOffset()) }
        if (idx < 0) return null
        return (if (next) ranges.getLoopNext(idx) else ranges.getLoopPrevious(idx)).startInnerOffset
    }
}
