import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

val generatedMainDir = layout.buildDirectory.dir("generated/sources/webConfig/jsMain/kotlin")
val generatedTestDir = layout.buildDirectory.dir("generated/sources/shippedHtml/jsTest/kotlin")

fun String.asKotlinString(): String = buildString {
    append('"')
    for (character in this@asKotlinString) {
        when (character) {
            '\\' -> append("\\\\")
            '"' -> append("\\\"")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            '$' -> append("\\$")
            else -> append(character)
        }
    }
    append('"')
}

val generateWebConfig = tasks.register("generateWebConfig") {
    val localEnvironment = layout.projectDirectory.file("../.env")
    val exampleEnvironment = layout.projectDirectory.file("../.env.example")
    inputs.file(localEnvironment).optional()
    inputs.file(exampleEnvironment)
    outputs.dir(generatedMainDir)

    doLast {
        val source = localEnvironment.asFile.takeIf { it.isFile } ?: exampleEnvironment.asFile
        val values = source.readLines()
            .map(String::trim)
            .filter { it.isNotEmpty() && !it.startsWith('#') && '=' in it }
            .associate { line -> line.substringBefore('=').trim() to line.substringAfter('=').trim() }
        val apiBaseUrl = requireNotNull(values["API_BASE_URL"]) { "API_BASE_URL is missing from ${source.absolutePath}" }
        val wsUrl = requireNotNull(values["WS_URL"]) { "WS_URL is missing from ${source.absolutePath}" }
        val output = generatedMainDir.get().file("com/hesham/chatting/web/config/WebBuildConfig.kt").asFile
        output.parentFile.mkdirs()
        output.writeText(
            """package com.hesham.chatting.web.config

internal const val API_BASE_URL: String = ${apiBaseUrl.asKotlinString()}
internal const val WS_URL: String = ${wsUrl.asKotlinString()}
""",
        )
    }
}

val generateShippedHtml = tasks.register("generateShippedHtml") {
    val resources = layout.projectDirectory.dir("src/jsMain/resources")
    val htmlFiles = listOf("index.html", "login.html", "chat.html").associateWith { resources.file(it) }
    inputs.files(htmlFiles.values)
    outputs.dir(generatedTestDir)

    doLast {
        val output = generatedTestDir.get().file("com/hesham/chatting/web/test/ShippedHtml.kt").asFile
        output.parentFile.mkdirs()
        output.writeText(
            """package com.hesham.chatting.web.test

internal object ShippedHtml {
    const val REGISTER: String = ${htmlFiles.getValue("index.html").asFile.readText().asKotlinString()}
    const val LOGIN: String = ${htmlFiles.getValue("login.html").asFile.readText().asKotlinString()}
    const val CHAT: String = ${htmlFiles.getValue("chat.html").asFile.readText().asKotlinString()}
}
""",
        )
    }
}

plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
    jvmToolchain(21)

    js(IR) {
        outputModuleName.set("webapp")
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                    useConfigDirectory(project.projectDir.resolve("karma.config.d"))
                }
            }
            commonWebpackConfig {
                // webpack.config.d/dev-server.js separately pins the bind host.
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    port = 3000
                }
            }
        }
        binaries.executable()
    }

    sourceSets {
        getByName("jsMain") {
            kotlin.srcDir(generatedMainDir)
            dependencies {
                implementation(libs.chatting.shared)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        getByName("jsTest") {
            kotlin.srcDir(generatedTestDir)
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}

tasks.named("compileKotlinJs").configure { dependsOn(generateWebConfig) }
tasks.named("compileTestKotlinJs").configure { dependsOn(generateShippedHtml) }
