package com.magidc.ideavim.anyobject.utils

import com.maddyhome.idea.vim.api.VimEditor
import org.treesitter.TSNode
import java.util.stream.IntStream

class TSModelExtensions {
    companion object {
        fun TSNode.toText(editor: VimEditor, maxSize: Int = 50): String {
            val string = String(editor.text().toString().toByteArray().copyOfRange(startByte, endByte))
            return string.take(maxSize)
        }

        fun TSNode.isEqual(a: TSNode?) = (a?.isNull == false) && (a.startByte == startByte) && (a.endByte == endByte)

        fun TSNode.lastNamedLeafOrSelf(): TSNode {
            if (namedChildCount == 0) return this
            return getNamedChild(namedChildCount - 1).lastNamedLeafOrSelf()
        }

        fun TSNode.lastLeafOrSelf(): TSNode {
            if (childCount == 0) return this
            return getChild(childCount - 1).lastLeafOrSelf()
        }

        fun TSNode.parentPrevNamedSibling(): TSNode? {
            if (parent.isNull) return null
            return if (parent.prevNamedSibling.isNull) parent.parentPrevNamedSibling() else parent.prevNamedSibling
        }

        fun TSNode.prevNamedLeaf(): TSNode? {
            return if (prevNamedSibling.isNull) parent.takeIf { !it.isNull } else prevNamedSibling?.lastNamedLeafOrSelf()
        }

        @Suppress("unused")
        fun TSNode.prevLeaf(): TSNode? {
            return if (prevSibling.isNull) parent.takeIf { !it.isNull } else prevSibling?.lastLeafOrSelf()
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

        fun TSNode.nextNamedLeaf(): TSNode? {
            if (namedChildCount > 0) return getNamedChild(0)
            return if (nextNamedSibling.isNull) parent.nextNamed() else nextNamedSibling
        }

        fun TSNode.nextLeaf(): TSNode? {
            if (childCount > 0) return getChild(0)
            return if (nextSibling.isNull) parent.next() else nextSibling
        }

        fun TSNode.getFirstNamedChildWithGrammar(grammars: Collection<String>, byteOffset: Int = this.startByte): TSNode? {
            val nodeDeque = ArrayDeque<TSNode>()
            nodeDeque.add(this)
            while (nodeDeque.isNotEmpty()) {
                val node = nodeDeque.removeFirst()
                if (node.endByte >= byteOffset && grammars.contains(node.grammarType)) return node
                IntStream.range(0, node.namedChildCount).mapToObj { node.getNamedChild(it) }.forEach(nodeDeque::add)
            }
            return null
        }

        fun TSNode.getFirstChildWithGrammar(grammars: Set<String>, byteOffset: Int = this.startByte): TSNode? {
            val nodeDeque = ArrayDeque<TSNode>()
            nodeDeque.add(this)
            while (nodeDeque.isNotEmpty()) {
                val node = nodeDeque.removeFirst()
                if (node.endByte >= byteOffset && grammars.contains(node.grammarType)) return node
                IntStream.range(0, node.childCount).mapToObj { node.getChild(it) }.forEach(nodeDeque::add)
            }
            return null
        }
    }
}