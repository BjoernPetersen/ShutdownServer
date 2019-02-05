import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.github.ben-manes.versions") version Plugin.VERSIONS

    application
    kotlin("jvm") version Plugin.KOTLIN
    kotlin("kapt") version Plugin.KOTLIN
    id("org.jetbrains.dokka") version Plugin.DOKKA

    idea
}

version = "3.1.0"

application {
    mainClassName = "net.bjoernpetersen.shutdown.Main"
}

dependencies {
    // Basics
    implementation(kotlin("stdlib-jdk8", version = Lib.KOTLIN))
    implementation(
        group = "io.github.microutils",
        name = "kotlin-logging",
        version = Lib.KOTLIN_LOGGING)
    implementation(group = "org.slf4j", name = "slf4j-api", version = Lib.SLF4J)
    runtime(group = "org.slf4j", name = "slf4j-simple", version = Lib.SLF4J)

    // Config
    implementation(group = "com.jdiazcano.cfg4k", name = "cfg4k-core", version = Lib.CFG4K)
    implementation(group = "com.jdiazcano.cfg4k", name = "cfg4k-yaml", version = Lib.CFG4K)
    implementation(kotlin("reflect", version = Lib.KOTLIN))
    implementation(
        group = "com.fasterxml.jackson.core",
        name = "jackson-databind",
        version = Lib.JACKSON)
    implementation(
        group = "com.fasterxml.jackson.module",
        name = "jackson-module-kotlin",
        version = Lib.JACKSON)
    implementation(
        group = "com.fasterxml.jackson.dataformat",
        name = "jackson-dataformat-yaml",
        version = Lib.JACKSON)
    implementation(group = "org.antlr", name = "ST4", version = Lib.STRING_TEMPLATE)

    // Vertx
    implementation(group = "io.vertx", name = "vertx-web", version = Lib.VERTX)
    implementation(group = "io.vertx", name = "vertx-lang-kotlin", version = Lib.VERTX) {
        exclude(group = "org.jetbrains.kotlin")
    }

    // Dependency injection
    implementation(
        group = "com.google.dagger",
        name = "dagger",
        version = Lib.DAGGER)
    kapt(
        group = "com.google.dagger",
        name = "dagger-compiler",
        version = Lib.DAGGER)

    testRuntime(
        group = "org.junit.jupiter",
        name = "junit-jupiter-engine",
        version = Lib.JUNIT)
    testImplementation(
        group = "org.junit.jupiter",
        name = "junit-jupiter-api",
        version = Lib.JUNIT)
    testImplementation(group = "io.vertx", name = "vertx-junit5", version = Lib.VERTX)
}

tasks {
    withType(KotlinCompile::class) {
        kotlinOptions.jvmTarget = "1.8"
    }

    "test"(Test::class) {
        useJUnitPlatform()
    }

    "dokka"(DokkaTask::class) {
        // TODO maybe switch to javadoc (or another) format
        outputFormat = "html"
        outputDirectory = "$buildDir/javadoc"
    }

    "processResources"(ProcessResources::class) {
        filesMatching("**/version.properties") {
            filter {
                it.replace("%APP_VERSION%", version.toString())
            }
        }
    }
}

repositories {
    jcenter()
}

idea {
    module {
        isDownloadJavadoc = true
    }
}
