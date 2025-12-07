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
            val fileExtension: String,
            val tsLanguage: (() -> TSLanguage)?,
        ) {
            override fun toString(): String = name
        }

        private val UNKNOWN_LANGUAGE = TSLanguageInfo("UNKNOWN", "", "", null)
        private val languageMap: Map<String, TSLanguageInfo> = mapOf(
            "JAVA" to TSLanguageInfo("JAVA", "intellij", "java", { TreeSitterJava() }),
            "KOTLIN" to TSLanguageInfo("KOTLIN", "intellij", "kt", { TreeSitterKotlin() }),
            "CLOJURE" to TSLanguageInfo("CLOJURE", "intellij", "clj", { TreeSitterClojure() }),
            "SCALA" to TSLanguageInfo("SCALA", "intellij", "scala", { TreeSitterScala() }),
            "C#" to TSLanguageInfo("C#", "rider", "cs", { TreeSitterCSharp() }),
            "RUST" to TSLanguageInfo("RUST", "rustrover", "rs", { TreeSitterRust() }),
            "GO" to TSLanguageInfo("GO", "goland", "go", { TreeSitterGo() }),
            "PYTHON" to TSLanguageInfo("PYTHON", "pycharm", "py", { TreeSitterPython() }),
            "PHP" to TSLanguageInfo("PHP", "phpstorm", "php", { TreeSitterPhp() }),
            "HTML" to TSLanguageInfo("HTML", "webstorm", "html", { TreeSitterHtml() }),
            "CSS" to TSLanguageInfo("CSS", "webstorm", "css", { TreeSitterCss() }),
            "ECMAScript 6" to TSLanguageInfo("ECMAScript 6", "webstorm", "js", { TreeSitterJavascript() }),
            "TYPESCRIPT" to TSLanguageInfo("TYPESCRIPT", "webstorm", "ts", { TreeSitterTypescript() }),
            "OBJECTIVE-C" to TSLanguageInfo("OBJECTIVE-C", "appcode", "m", { TreeSitterObjc() }),
            "SWIFT" to TSLanguageInfo("SWIFT", "appcode", "swift", { TreeSitterSwift() }),
            "C/C++" to TSLanguageInfo("C/C++", "clion", "cpp", { TreeSitterCpp() }),
            "R" to TSLanguageInfo("R", "", "r", { TreeSitterR() }),
            "SQL" to TSLanguageInfo("SQL", "", "sql", { TreeSitterSql() }),
            "JSON" to TSLanguageInfo("JSON", "", "json", { TreeSitterJson() }),
            "YAML" to TSLanguageInfo("YAML", "", "yaml", { TreeSitterYaml() })
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
            return languageMap.values.firstOrNull { it.fileExtension == extension }
        }

        private fun getDefaultAppLanguage(): TSLanguageInfo? {
            val ideName = ApplicationInfo.getInstance().fullApplicationName.lowercase()
            return languageMap.values.firstOrNull { ideName.contains(it.appName) }
        }

        fun getLanguage(editor: VimEditor): TSLanguageInfo {
            getPSILanguage(editor)?.let { languageMap[it.uppercase()] }?.let { return it }
            getLanguageByFileExtension(editor)?.let { return it }
            getDefaultAppLanguage()?.let { return it }
            return UNKNOWN_LANGUAGE
        }
    }
}