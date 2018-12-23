import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm") version Version.KOTLIN
    kotlin("kapt") version Version.KOTLIN
    id("org.jetbrains.dokka") version Version.DOKKA

    idea
}

version = "1.1.0"

application {
    mainClassName = "net.bjoernpetersen.shutdown.Main"
}

dependencies {
    // Basics
    implementation(kotlin("stdlib-jdk8", version = Version.KOTLIN))
    implementation(
        group = "io.github.microutils",
        name = "kotlin-logging",
        version = Version.KOTLIN_LOGGING)
    runtime(group = "org.slf4j", name = "slf4j-simple", version = Version.SLF4J)

    // Config
    implementation(group = "com.jdiazcano.cfg4k", name = "cfg4k-core", version = Version.CFG4K)
    implementation(group = "com.jdiazcano.cfg4k", name = "cfg4k-yaml", version = Version.CFG4K)
    implementation(kotlin("reflect", version = Version.KOTLIN))

    // Vertx
    implementation(group = "io.vertx", name = "vertx-web", version = Version.VERTX)
    implementation(group = "io.vertx", name = "vertx-lang-kotlin", version = Version.VERTX) {
        exclude(group = "org.jetbrains.kotlin")
    }

    // Dependency injection
    implementation(
        group = "com.google.dagger",
        name = "dagger",
        version = Version.DAGGER)
    kapt(
        group = "com.google.dagger",
        name = "dagger-compiler",
        version = Version.DAGGER)

    testRuntime(
        group = "org.junit.jupiter",
        name = "junit-jupiter-engine",
        version = Version.JUNIT)
    testImplementation(
        group = "org.junit.jupiter",
        name = "junit-jupiter-api",
        version = Version.JUNIT)
    testImplementation(group = "io.vertx", name = "vertx-junit5", version = Version.VERTX)
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
}

repositories {
    jcenter()
}

idea {
    module {
        isDownloadJavadoc = true
    }
}
