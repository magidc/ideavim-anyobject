package com.magidc.ideavim.anyobject.handlers

import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.util.elementType
import com.intellij.psi.util.siblings
import com.maddyhome.idea.vim.api.VimEditor
import com.magidc.ideavim.anyobject.model.Selection
import java.nio.file.Path

abstract class AbstractPSIBasedHandler(isInner: Boolean) : Handler(isInner) {
    override fun findSelection(editor: VimEditor): Selection? {
        val projectManager = ProjectManager.getInstance()
        if (null == projectManager || projectManager.openProjects.isEmpty()) return null
        val project = projectManager.openProjects[0]
        val vimVirtualFile = editor.getVirtualFile() ?: return null
        val virtualFile = VirtualFileManager.getInstance().findFileByNioPath(Path.of(vimVirtualFile.path)) ?: return null

        val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
        val element = psiFile?.findElementAt(editor.currentCaret().offset) ?: return null
        val itemElement = findItemPSIFile(element) ?: return null
        val collectionDelimiters = setOf(itemElement.parent.firstChild.text, itemElement.parent.lastChild.text)

        val leftDelimiter = itemElement.siblings(false)
            .filter { it.startOffsetInParent < itemElement.startOffsetInParent }
            .filter { it.text.isNotBlank() }
            .filter { !collectionDelimiters.contains(it.text) }
            .firstOrNull()

        val itemLeftDelimiter = leftDelimiter?.text ?: ""

        val itemRightDelimiter = itemElement.siblings()
            .filter { it.startOffsetInParent > itemElement.startOffsetInParent }
            .filter { it.text.isNotBlank() }
            .first().text?.takeIf { !collectionDelimiters.contains(it) } ?: ""

        if (isInner)
            return Selection(
                itemElement.textRange.startOffset,
                itemElement.textRange.endOffset,
                itemLeftDelimiter,
                itemRightDelimiter
            )
        return Selection(
            leftDelimiter?.textRange?.startOffset ?: itemElement.textRange.startOffset,
            itemElement.textRange.endOffset,
            itemLeftDelimiter,
            itemRightDelimiter
        )
    }

    private fun findItemPSIFile(psiElement: PsiElement): PsiElement? {
        var currentElement = psiElement
        while (currentElement.parent != null) {
            val elementName = (currentElement.parent.elementType ?: break).toString()
            if (acceptElementTypeName(elementName)) return currentElement
            currentElement = currentElement.parent
        }
        return null
    }

    protected abstract fun acceptElementTypeName(elementName: String): Boolean
}