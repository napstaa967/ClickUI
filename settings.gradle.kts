pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases/")
    }
}

rootProject.name = "ClickUI"

plugins {
    id("dev.kikugie.stonecutter") version "0.9"
}

stonecutter {
    create(rootProject) {
        fun version(version: String, vararg loaders: String) {
            loaders.forEach {
                if (it == "fabric-old") {
                    this.version("$version-fabric", version)
                        .buildscript = "build.$it.gradle.kts"
                } else {
                    this.version("$version-$it", version)
                        .buildscript = "build.$it.gradle.kts"
                }
            }
        }
        version("1.20.1", "fabric-old", "forge")
        version("1.21.1", "fabric-old", "neoforge")
        version("26.1", "fabric")
        vcsVersion = "1.20.1-fabric"
    }

}