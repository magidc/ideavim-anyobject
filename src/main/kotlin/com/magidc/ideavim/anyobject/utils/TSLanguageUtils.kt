package com.magidc.ideavim.anyobject.utils

import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiManager
import com.maddyhome.idea.vim.api.VimEditor
import com.magidc.ideavim.anyobject.handlers.base.getCaretOffset
import org.treesitter.TSLanguage
import org.treesitter.TreeSitterCSharp
import org.treesitter.TreeSitterClojure
import org.treesitter.TreeSitterCpp
import org.treesitter.TreeSitterCss
import org.treesitter.TreeSitterGo
import org.treesitter.TreeSitterHtml
import org.treesitter.TreeSitterJava
import org.treesitter.TreeSitterJavascript
import org.treesitter.TreeSitterJson
import org.treesitter.TreeSitterKotlin
import org.treesitter.TreeSitterObjc
import org.treesitter.TreeSitterPhp
import org.treesitter.TreeSitterPython
import org.treesitter.TreeSitterR
import org.treesitter.TreeSitterRuby
import org.treesitter.TreeSitterRust
import org.treesitter.TreeSitterScala
import org.treesitter.TreeSitterSql
import org.treesitter.TreeSitterSwift
import org.treesitter.TreeSitterTypescript
import org.treesitter.TreeSitterYaml
import java.nio.file.Path

class TSLanguageUtils {
    enum class TSBlockType {
        BRACES,
        INDENTED,
        END,
        UNKNOWN
    }

    companion object {
        class TSLanguageInfo(
            val name: String,
            val appName: String,
            val tsLanguage: (() -> TSLanguage)?,
            val fileExtensions: Set<String>,
            val blockType: TSBlockType = TSBlockType.UNKNOWN
        ) {
            override fun toString(): String = name
        }

        private val UNKNOWN_LANGUAGE = TSLanguageInfo("UNKNOWN", "", null, emptySet(), TSBlockType.UNKNOWN)
        val JAVA = TSLanguageInfo("JAVA", "intellij", { TreeSitterJava() }, setOf("java"), TSBlockType.BRACES)
        val PYTHON = TSLanguageInfo("PYTHON", "pycharm", { TreeSitterPython() }, setOf("py"), TSBlockType.INDENTED)
        val CPP = TSLanguageInfo("C/C++", "clion", { TreeSitterCpp() }, setOf("cpp"), TSBlockType.BRACES)
        val PHP = TSLanguageInfo("PHP", "phpstorm", { TreeSitterPhp() }, setOf("php"), TSBlockType.BRACES)
        val RUBY = TSLanguageInfo("RUBY", "rubymine", { TreeSitterRuby() }, setOf("rb"), TSBlockType.END)
        private val languageMap: Map<String, TSLanguageInfo> = mapOf(
            "JAVA" to JAVA,
            "KOTLIN" to TSLanguageInfo("KOTLIN", "intellij", { TreeSitterKotlin() }, setOf("kt"), TSBlockType.BRACES),
            "CLOJURE" to TSLanguageInfo("CLOJURE", "intellij", { TreeSitterClojure() }, setOf("clj"), TSBlockType.UNKNOWN),
            "SCALA" to TSLanguageInfo("SCALA", "intellij", { TreeSitterScala() }, setOf("scala"), TSBlockType.BRACES),
            "C#" to TSLanguageInfo("C#", "rider", { TreeSitterCSharp() }, setOf("cs"), TSBlockType.BRACES),
            "RUST" to TSLanguageInfo("RUST", "rustrover", { TreeSitterRust() }, setOf("rs"), TSBlockType.BRACES),
            "RUBY" to RUBY,
            "GO" to TSLanguageInfo("GO", "goland", { TreeSitterGo() }, setOf("go"), TSBlockType.BRACES),
            "PYTHON" to PYTHON,
            "PHP" to PHP,
            "HTML" to TSLanguageInfo("HTML", "webstorm", { TreeSitterHtml() }, setOf("html", "htm"), TSBlockType.UNKNOWN),
            "CSS" to TSLanguageInfo("CSS", "webstorm", { TreeSitterCss() }, setOf("css"), TSBlockType.BRACES),
            "ECMAScript 6" to TSLanguageInfo("ECMAScript 6", "webstorm", { TreeSitterJavascript() }, setOf("js"), TSBlockType.BRACES),
            "TYPESCRIPT" to TSLanguageInfo("TYPESCRIPT", "webstorm", { TreeSitterTypescript() }, setOf("ts"), TSBlockType.BRACES),
            "OBJECTIVE-C" to TSLanguageInfo("OBJECTIVE-C", "appcode", { TreeSitterObjc() }, setOf("m"), TSBlockType.BRACES),
            "SWIFT" to TSLanguageInfo("SWIFT", "appcode", { TreeSitterSwift() }, setOf("swift"), TSBlockType.BRACES),
            "C/C++" to CPP,
            "R" to TSLanguageInfo("R", "", { TreeSitterR() }, setOf("r"), TSBlockType.BRACES),
            "SQL" to TSLanguageInfo("SQL", "", { TreeSitterSql() }, setOf("sql"), TSBlockType.UNKNOWN),
            "JSON" to TSLanguageInfo("JSON", "", { TreeSitterJson() }, setOf("json"), TSBlockType.BRACES),
            "YAML" to TSLanguageInfo("YAML", "", { TreeSitterYaml() }, setOf("yaml", "yml"), TSBlockType.INDENTED)
        )

        private fun getPSILanguage(editor: VimEditor): String? {
            val vimVirtualFile = editor.getVirtualFile() ?: return null
            val projectManager = ProjectManager.getInstance()
            if (null == projectManager || projectManager.openProjects.isEmpty()) return null
            val project = projectManager.openProjects[0]
            val virtualFile = VirtualFileManager.getInstance().findFileByNioPath(Path.of(vimVirtualFile.path)) ?: return null
            return PsiManager.getInstance(project).findFile(virtualFile)?.findElementAt(editor.getCaretOffset())?.language?.displayName
        }

        private fun getLanguageByFileExtension(editor: VimEditor): TSLanguageInfo? {
            val filePath = editor.getVirtualFile()?.path ?: return null
            val extension = filePath.substringAfterLast('.', "")
            return languageMap.values.firstOrNull { it.fileExtensions.contains(extension) }
        }

        private fun getDefaultAppLanguage(): TSLanguageInfo? {
            val ideName = ApplicationInfo.getInstance().fullApplicationName.lowercase()
            return languageMap.values.firstOrNull { ideName.contains(it.appName) }
        }

        fun getLanguage(editor: VimEditor): TSLanguageInfo {
            getLanguageByFileExtension(editor)?.let { return it }
            getPSILanguage(editor)?.let { languageMap[it.uppercase()] }?.let { return it }
            getDefaultAppLanguage()?.let { return it }
            return UNKNOWN_LANGUAGE
        }
    }
}