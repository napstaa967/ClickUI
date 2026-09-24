plugins {
    id("java")
    id("net.neoforged.moddev") version "2.0.141"
    id("maven-publish")
}
val modVersion = property("mod.version").toString()
val minecraftVersion = property("mod.minecraft_version").toString()
val loader = stonecutter.current.project.substringAfterLast('-')

group = project.property("maven_group").toString()
version = "$modVersion+$minecraftVersion-$loader"

repositories {
    mavenCentral()
    maven("https://thedarkcolour.github.io/KotlinForForge/")
}

sourceSets {
    create("testmod") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

configurations {
    testRuntimeClasspath { extendsFrom(runtimeClasspath) }
    testCompileClasspath { extendsFrom(compileClasspath) }
}

neoForge {
    version = "${property("deps.neoforge")}"

    addModdingDependenciesTo(sourceSets["testmod"])

    runs {
        register("client") {
            client()
            gameDirectory = rootProject.file("runs/neoforge")
            ideName = "Neoforge Client (${stonecutter.active?.version})"
            programArgument("--username=ClickToPlay")
        }
        register("server") {
            server()
            gameDirectory = rootProject.file("runs/neoforge")
            ideName = "Neoforge Server (${stonecutter.active?.version})"
        }
        register("testmodClient") {
            client()
            gameDirectory = rootProject.file("runs/neoforge")
            ideName = "Testmod Client (${stonecutter.active?.version})"
            sourceSet.set(sourceSets["testmod"])
        }
    }

    mods {
        register(property("mod.id").toString()) {
            sourceSet(sourceSets["main"])
        }
        register("clickuitestmod") {
            sourceSet(sourceSets["testmod"])
        }
    }
}

dependencies {
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
    compileOnly("org.jetbrains:annotations:24.0.1")
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

//mixin {
//    add(sourceSets.main.get(), "${property("mod.id")}.mixins.refmap.json")
//    config("${property("mod.id")}.mixins.json")
//}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

base {
    archivesName.set(property("archives_base_name").toString())
}

tasks.test {
    useJUnitPlatform()
}

tasks.processResources {
    val properties = mapOf(
        "mod_version" to modVersion,
        "minecraft_version" to minecraftVersion,
    )
    filesMatching(listOf("META-INF/neoforge.mods.toml", "META-INF/mods.toml")) {
        expand(properties)
    }
    inputs.properties(properties)
}

tasks.named("createMinecraftArtifacts") {
    dependsOn(tasks.named("stonecutterGenerate"))
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

stonecutter {
    replacements.string(sc.current.parsed >= "26.1") {
        replace("GuiGraphics", "GuiGraphicsExtractor")
        replace("ResourceLocation", "Identifier")
    }
}