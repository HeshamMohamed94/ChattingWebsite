import java.util.Properties

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
}

rootProject.name = "ChattingWebsite"

include(":webapp")

val localPropertiesFile = file("local.properties")
if (localPropertiesFile.isFile) {
    val localProperties = Properties().apply {
        localPropertiesFile.inputStream().use(::load)
    }
    val sharedProjectPath = localProperties.getProperty("sharedProjectPath")
    if (!sharedProjectPath.isNullOrBlank()) {
        val sharedProjectDirectory = file(sharedProjectPath)
        require(sharedProjectDirectory.isDirectory) {
            "sharedProjectPath does not point to a directory: ${sharedProjectDirectory.absolutePath}"
        }
        includeBuild(sharedProjectDirectory)
    }
}
