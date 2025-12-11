package com.magidc.ideavim.anyobject.handlers.base

import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.common.TextRange
import com.magidc.ideavim.anyobject.utils.LRUCache
import com.magidc.ideavim.anyobject.utils.TSDocument
import org.treesitter.TSNode
import java.util.stream.IntStream


abstract class AbstractTSBasedHandler : BaseSelectionHandler, BaseJumpHandler {

    companion object {
        private val documentCache = LRUCache<String, TSDocument>(5) { _, v -> v.editor.document.removeChangeListener(v) }

        @Suppress("unused")
        fun TSNode.toText(editor: VimEditor, textLimit: Int = 20): String {
            val string = String(editor.text().toString().toByteArray().copyOfRange(startByte, endByte))
            return "${grammarType}: ${string.take(textLimit)}"
        }

        fun TSNode.isEqual(a: TSNode?) = (a?.isNull == false) && (a.startByte == startByte) && (a.endByte == endByte)

        fun TSNode.lastNamedLeafOrSelf(): TSNode {
            if (namedChildCount == 0) return this
            return getNamedChild(namedChildCount - 1).lastNamedLeafOrSelf()
        }

        fun TSNode.parentPrevNamedSibling(): TSNode? {
            if (parent.isNull) return null
            return if (parent.prevNamedSibling.isNull) parent.parentPrevNamedSibling() else parent.prevNamedSibling
        }

        fun TSNode.prevNamedLeaf(): TSNode? {
            return if (prevNamedSibling.isNull) parent.takeIf { !it.isNull } else prevNamedSibling?.lastNamedLeafOrSelf()
        }

        fun TSNode.nextNamed(): TSNode? {
            if (nextNamedSibling.isNull)
                return if (parent.isNull) null else parent.nextNamed()
            return nextNamedSibling
        }

        private fun TSNode.next(): TSNode? {
            if (nextSibling.isNull)
                return if (parent.isNull) null else parent.next()
            return nextSibling
        }

        fun TSNode.nextNamedChild(): TSNode? {
            if (namedChildCount > 0) return getNamedChild(0)
            return if (nextNamedSibling.isNull) parent.nextNamed() else nextNamedSibling
        }

        fun TSNode.nextChild(): TSNode? {
            if (childCount > 0) return getChild(0)
            return if (nextSibling.isNull) parent.next() else nextSibling
        }

        fun TSNode.getFirstNamedChildWithGrammar(grammars: Collection<String>, offset: Int = this.startByte): TSNode? {
            val nodeDeque = ArrayDeque<TSNode>()
            nodeDeque.add(this)
            while (nodeDeque.isNotEmpty()) {
                val node = nodeDeque.removeFirst()
                if (node.endByte >= offset && grammars.contains(node.grammarType)) return node
                IntStream.range(0, node.namedChildCount).mapToObj { node.getNamedChild(it) }.forEach(nodeDeque::add)
            }
            return null
        }

        fun TSNode.getFirstChildWithGrammar(grammars: Set<String>, offset: Int = this.startByte): TSNode? {
            val nodeDeque = ArrayDeque<TSNode>()
            nodeDeque.add(this)
            while (nodeDeque.isNotEmpty()) {
                val node = nodeDeque.removeFirst()
                if (node.endByte >= offset && grammars.contains(node.grammarType)) return node
                IntStream.range(0, node.childCount).mapToObj { node.getChild(it) }.forEach(nodeDeque::add)
            }
            return null
        }
    }

    protected open val innerBlockTypes: Collection<String> = setOf("block")
    protected abstract val targetTypes: Set<String>

    protected fun getTSDocument(editor: VimEditor): TSDocument = documentCache.getOrPut(editor.getVirtualFile()?.path ?: "") { TSDocument(editor) }

    protected open fun acceptNode(node: TSNode): Boolean = !node.isNull && node.isNamed && targetTypes.contains(node.grammarType)

    override fun findSelection(editor: VimEditor, inner: Boolean, size: Int): TextRange? {
        val tsDocument = getTSDocument(editor)
        val objectNode = tsDocument.findSelectionNode { acceptNode(it) } ?: return null
        if (!inner) return tsDocument.toTextRange(objectNode)
        return findInnerBlockRange(objectNode, editor.getCareOffset(), tsDocument)
    }

    override fun allowsCountSelection(): Boolean = false

    protected open fun findInnerBlockRange(node: TSNode, offset: Int, tsDocument: TSDocument): TextRange? {
        return node.getFirstNamedChildWithGrammar(innerBlockTypes, offset)
            ?.takeIf { it.namedChildCount > 0 }
            ?.let { tsDocument.toTextRange(it.getNamedChild(0), it.getNamedChild(it.namedChildCount - 1)) }
    }

    final override fun findJumpElementStartOffset(editor: VimEditor, forward: Boolean): Int? {
        return getTSDocument(editor).findJumpElementOffset({ acceptNode(it) }, forward)
    }
}