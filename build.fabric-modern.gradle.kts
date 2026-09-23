plugins {
    id("java")
    id("net.fabricmc.fabric-loom-remap") version "1.17-SNAPSHOT"
    id("maven-publish")
}
val modVersion = property("mod.version").toString()
val minecraftVersion = property("mod.minecraft_version").toString()
val loader = stonecutter.current.project.substringAfterLast('-')

group = project.property("maven_group").toString()
version = "$modVersion+$minecraftVersion-$loader"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://maven.parchmentmc.org")
}

sourceSets {
    create("testmod") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

loom {
    runConfigs.all {
        generateRunConfig.set(true)
        runDirectory.set(rootProject.file("runs/fabric"))
//        if (runtimeEnvironment.get() == "client") {
//            programArguments.set(listOf("--username=ClickToPlay"))
//        }
    }
    createRemapConfigurations(sourceSets["testmod"])
    mods {
        create("clickui-testmod") {
            sourceSet(sourceSets["testmod"])
        }
    }
    runs {
        create("testmodClient") {
            client()
            displayName = "Testmod Client"
            sourceSet = "testmod"
        }
    }
}

java {
    if (sc.current.parsed >= "1.21") {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    } else {
        toolchain.languageVersion.set(JavaLanguageVersion.of(17))
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

base {
    archivesName.set(property("archives_base_name").toString())
}

configurations {
    named("testmodCompileClasspath") {
        extendsFrom(named("compileClasspath").get())
    }

    named("testmodRuntimeClasspath") {
        extendsFrom(named("runtimeClasspath").get())
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings(loom.layered() {
        officialMojangMappings()
        if (sc.current.parsed >= "1.21") {
            parchment("org.parchmentmc.data:parchment-1.21.1:2024.11.17@zip")
        } else {
            parchment("org.parchmentmc.data:parchment-1.20.1:2023.09.03@zip")
        }
    })

    // Testmod-only dependencies
    "modTestmodImplementation"("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    "modTestmodImplementation"("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")

    compileOnly("org.jetbrains:annotations:24.0.1")
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.processResources {
    dependsOn(tasks.named("stonecutterGenerate"))
    val properties = mapOf(
        "mod_version" to modVersion,
        "minecraft_version" to minecraftVersion,
        "fabric_loader_version" to project.property("deps.fabric_loader")
    )

    filesMatching(listOf("fabric.mod.json")) {
        expand(properties)
    }
    inputs.properties(properties)
}

tasks.test {
    useJUnitPlatform()
}

fabricApi {
    configureDataGeneration {
        val currentVersion = sc.current.version.substringBeforeLast("-")
        outputDirectory = rootProject.file("versions/datagen/$currentVersion/src/main/generated")
        client = true
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = group.toString()
            artifactId = "clickui"
            version = version.toString()
        }
    }
}