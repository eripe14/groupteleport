import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    `java-library`

    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("com.gradleup.shadow") version "9.0.0-beta12"
    id("de.eldoria.plugin-yml.paper") version "0.7.1"
}

group = "pl.kdronia"
version = "1.0-SNAPSHOT"

val mainPackage = "pl.kdronia.groupteleport"
val projectPrefix = "groupteleport"

repositories {
    mavenCentral()

    maven("https://storehouse.okaeri.eu/repository/maven-public/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/releases/")
    maven("https://jitpack.io")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://repo.eternalcode.pl/releases")
    maven("https://maven.enginehub.org/repo/")
}

dependencies {
    // -- paper --
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")

    // -- configs --
    implementation("eu.okaeri:okaeri-configs-yaml-bukkit:5.0.6")
    implementation("eu.okaeri:okaeri-configs-serdes-bukkit:5.0.6")
    implementation("eu.okaeri:okaeri-configs-serdes-commons:5.0.6")
    implementation("eu.okaeri:okaeri-configs-json-simple:5.0.6")

    // -- commons
    implementation("net.kyori:adventure-platform-bukkit:4.3.1")
    implementation("net.kyori:adventure-text-minimessage:4.22.0")

    // -- notifications --
    implementation("com.eternalcode:multification-bukkit:1.1.4")
    implementation("com.eternalcode:multification-okaeri:1.1.4")

    // -- commands --
    implementation("dev.rollczi:litecommands-bukkit:3.9.7")

    // -- world guard --
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.13")
    compileOnly("net.raidstone:WorldGuardEvents:1.18.1")
}

paper {
    name = "svhard-groupteleport"
    version = "${project.version}"
    author = "Karol Dronia"
    prefix = "svhard-groupteleport"
    apiVersion = "1.21"

    main = "$mainPackage.GroupTeleportPlugin"

    serverDependencies {
        register("WorldGuard") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = true
            joinClasspath = true
        }
        register("WorldGuardEvents") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = true
            joinClasspath = true
        }
    }
}

tasks.shadowJar {
    archiveFileName.set("svhard-groupteleport v${project.version}.jar")

    exclude(
        "org/intellij/lang/annotations/**",
        "org/jetbrains/annotations/**",
        "META-INF/**"
    )

    listOf(
        "dev.rollczi",
        "eu.okaeri",
        "panda",
        "org.yaml",
        "com.eternalcode.commons",
    ).forEach { relocate(it, "$mainPackage.$it") }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
}

tasks.runServer {
    version("1.21.4")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}