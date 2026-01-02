package com.magidc.ideavim.anyobject.utils

import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiManager
import com.maddyhome.idea.vim.api.VimEditor
import com.magidc.ideavim.anyobject.handlers.base.getCareOffset
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
    companion object {
        class TSLanguageInfo(
            val name: String,
            val appName: String,
            val tsLanguage: (() -> TSLanguage)?,
            val fileExtensions: Set<String>
        ) {
            override fun toString(): String = name
        }

        private val UNKNOWN_LANGUAGE = TSLanguageInfo("UNKNOWN", "", null, emptySet())
        private val languageMap: Map<String, TSLanguageInfo> = mapOf(
            "JAVA" to TSLanguageInfo("JAVA", "intellij", { TreeSitterJava() }, setOf("java")),
            "KOTLIN" to TSLanguageInfo("KOTLIN", "intellij", { TreeSitterKotlin() }, setOf("kt")),
            "CLOJURE" to TSLanguageInfo("CLOJURE", "intellij", { TreeSitterClojure() }, setOf("clj")),
            "SCALA" to TSLanguageInfo("SCALA", "intellij", { TreeSitterScala() }, setOf("scala")),
            "C#" to TSLanguageInfo("C#", "rider", { TreeSitterCSharp() }, setOf("cs")),
            "RUST" to TSLanguageInfo("RUST", "rustrover", { TreeSitterRust() }, setOf("rs")),
            "RUBY" to TSLanguageInfo("RUBY", "rubymine", { TreeSitterRuby() }, setOf("rb")),
            "GO" to TSLanguageInfo("GO", "goland", { TreeSitterGo() }, setOf("go")),
            "PYTHON" to TSLanguageInfo("PYTHON", "pycharm", { TreeSitterPython() }, setOf("py")),
            "PHP" to TSLanguageInfo("PHP", "phpstorm", { TreeSitterPhp() }, setOf("php")),
            "HTML" to TSLanguageInfo("HTML", "webstorm", { TreeSitterHtml() }, setOf("html", "htm")),
            "CSS" to TSLanguageInfo("CSS", "webstorm", { TreeSitterCss() }, setOf("css")),
            "ECMAScript 6" to TSLanguageInfo("ECMAScript 6", "webstorm", { TreeSitterJavascript() }, setOf("js")),
            "TYPESCRIPT" to TSLanguageInfo("TYPESCRIPT", "webstorm", { TreeSitterTypescript() }, setOf("ts")),
            "OBJECTIVE-C" to TSLanguageInfo("OBJECTIVE-C", "appcode", { TreeSitterObjc() }, setOf("m")),
            "SWIFT" to TSLanguageInfo("SWIFT", "appcode", { TreeSitterSwift() }, setOf("swift")),
            "C/C++" to TSLanguageInfo("C/C++", "clion", { TreeSitterCpp() }, setOf("cpp")),
            "R" to TSLanguageInfo("R", "", { TreeSitterR() }, setOf("r")),
            "SQL" to TSLanguageInfo("SQL", "", { TreeSitterSql() }, setOf("sql")),
            "JSON" to TSLanguageInfo("JSON", "", { TreeSitterJson() }, setOf("json")),
            "YAML" to TSLanguageInfo("YAML", "", { TreeSitterYaml() }, setOf("yaml", "yml"))
        )

        private fun getPSILanguage(editor: VimEditor): String? {
            val vimVirtualFile = editor.getVirtualFile() ?: return null
            val projectManager = ProjectManager.getInstance()
            if (null == projectManager || projectManager.openProjects.isEmpty()) return null
            val project = projectManager.openProjects[0]
            val virtualFile = VirtualFileManager.getInstance().findFileByNioPath(Path.of(vimVirtualFile.path)) ?: return null
            return PsiManager.getInstance(project).findFile(virtualFile)?.findElementAt(editor.getCareOffset())?.language?.displayName
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