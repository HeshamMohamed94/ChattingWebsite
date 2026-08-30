import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

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
        jsMain.dependencies {
            implementation(libs.chatting.shared)
        }
    }
}
