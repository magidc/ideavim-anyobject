import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java")
    kotlin("jvm") version "2.2.0"
    id("org.jetbrains.intellij.platform") version "2.7.2"
    id("org.jetbrains.changelog") version "2.3.0"
    id("com.diffplug.spotless") version "6.25.0"
    id("pmd")
    id("com.gradleup.shadow") version "9.1.0"
}

changelog {
    version.set(providers.gradleProperty("pluginVersion"))
    path.set(file("CHANGELOG.md").canonicalPath)
    header.set(provider { "[${version.get()}]" })
    headerParserRegex.set("""(\d+\.\d+\.\d+)""".toRegex())
    itemPrefix.set("-")
    keepUnreleasedSection.set(true)
//    unreleasedTerm.set("[Next]")
    groups.set(listOf(""))
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}


dependencies {
    implementation("org.reflections:reflections:0.10.2")
    testImplementation("org.assertj:assertj-core:3.25.3")
    testImplementation("junit:junit:4.13.2")
    intellijPlatform {
        create(providers.gradleProperty("platformType"), providers.gradleProperty("platformVersion"))
        bundledPlugins(providers.gradleProperty("platformBundledPlugins").map { it.split(',') })
        plugins(providers.gradleProperty("platformPlugins").map { it.split(',') })
        pluginVerifier()
        zipSigner()
        testFramework(TestFrameworkType.Platform)
    }
    // Treesitter
    implementation("io.github.bonede:tree-sitter:0.25.3")
    implementation("io.github.bonede:tree-sitter-java:0.23.4")
    implementation("io.github.bonede:tree-sitter-clojure:0.0.12")
    implementation("io.github.bonede:tree-sitter-r:main")
    implementation("io.github.bonede:tree-sitter-kotlin:0.3.8.1")
    implementation("io.github.bonede:tree-sitter-scala:0.23.3")
    implementation("io.github.bonede:tree-sitter-ruby:0.23.1")
    implementation("io.github.bonede:tree-sitter-c-sharp:0.23.1")
    implementation("io.github.bonede:tree-sitter-html:0.23.2")
    implementation("io.github.bonede:tree-sitter-css:0.23.1")
    implementation("io.github.bonede:tree-sitter-javascript:0.23.1")
    implementation("io.github.bonede:tree-sitter-typescript:0.23.2")
    implementation("io.github.bonede:tree-sitter-rust:0.23.1")
    implementation("io.github.bonede:tree-sitter-python:0.23.4")
    implementation("io.github.bonede:tree-sitter-r:main")
    implementation("io.github.bonede:tree-sitter-sql:gh-pages")
    implementation("io.github.bonede:tree-sitter-go:0.23.3")
    implementation("io.github.bonede:tree-sitter-objc:main")
    implementation("io.github.bonede:tree-sitter-swift:0.5.0")
    implementation("io.github.bonede:tree-sitter-php:0.23.11")
    implementation("io.github.bonede:tree-sitter-comment:0.1.0")
    implementation("io.github.bonede:tree-sitter-cpp:0.23.4")
    implementation("io.github.bonede:tree-sitter-json:0.24.8")
    implementation("io.github.bonede:tree-sitter-yaml:0.5.0")
}

intellijPlatform {
    pluginConfiguration {
        id = providers.gradleProperty("pluginId")
        name = providers.gradleProperty("pluginName")
        version = providers.gradleProperty("pluginVersion")
        description = providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"

            with(it.lines()) {
                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
            }
        }
        val changelog = project.changelog
        changeNotes = providers.gradleProperty("pluginVersion").map { pluginVersion ->
            with(changelog) {
                renderItem(
                    (getOrNull(pluginVersion) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
        }
    }
    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
        channels = providers.gradleProperty("pluginVersion")
            .map { listOf(it.substringAfter('-', "").substringBefore('.').ifEmpty { "default" }) }
    }
    pluginVerification {
        ides {
            recommended()
        }
    }
}

spotless {
    java {
        removeUnusedImports()
        googleJavaFormat("1.22.0")
            .aosp()
            .reflowLongStrings()
            .groupArtifact("com.google.googlejavaformat:google-java-format")
    }
    kotlin {
        ktlint()
        ktfmt().googleStyle().configure {
            it.setRemoveUnusedImport(true)
        }
        trimTrailingWhitespace()
        endWithNewline()
    }
}

pmd {
    isConsoleOutput = true
    toolVersion = "6.46.0"
    rulesMinimumPriority.set(5)
    ruleSetFiles = rootProject.files("pmd-config.xml")
    ruleSets = emptyList()
    isIgnoreFailures = false
}

tasks.named<Pmd>("pmdMain") {
    reports {
        html.required.set(true)
    }
}

kotlin {
    jvmToolchain(21)
}

tasks {
    buildSearchableOptions {
        enabled = false
    }

    signPlugin {
        if (System.getenv("CERTIFICATE_CHAIN") != null && System.getenv("PRIVATE_KEY") != null) {
            certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
            privateKey.set(System.getenv("PRIVATE_KEY"))
            password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
        } else if (System.getenv("CERTIFICATE_CHAIN_FILE") != null && System.getenv("PRIVATE_KEY_FILE") != null) {
            certificateChainFile.set(file(System.getenv("CERTIFICATE_CHAIN_FILE")))
            privateKeyFile.set(file(System.getenv("PRIVATE_KEY_FILE")))
            password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
        } else
            return@signPlugin
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
        dependsOn(patchChangelog)
    }
    @Suppress("unused")
    val runIntellij by intellijPlatformTesting.runIde.registering {
        localPath.set(file("/opt/jetbrains/intellij"))
    }

    @Suppress("unused")
    val runPyCharm by intellijPlatformTesting.runIde.registering {
        localPath.set(file("/opt/jetbrains/pycharm"))
    }

    @Suppress("unused")
    val runPHPStorm by intellijPlatformTesting.runIde.registering {
        localPath.set(file("/opt/jetbrains/phpstorm"))
    }

    @Suppress("unused")
    val runRider by intellijPlatformTesting.runIde.registering {
        localPath.set(file("/opt/jetbrains/rider"))
    }

    @Suppress("unused")
    val runCLion by intellijPlatformTesting.runIde.registering {
        localPath.set(file("/opt/jetbrains/clion"))
    }

    @Suppress("unused")
    val runRubyMine by intellijPlatformTesting.runIde.registering {
        localPath.set(file("/opt/jetbrains/rubymine"))
    }

    @Suppress("unused")
    val runWebStorm by intellijPlatformTesting.runIde.registering {
        localPath.set(file("/opt/jetbrains/webstorm"))
    }
}
