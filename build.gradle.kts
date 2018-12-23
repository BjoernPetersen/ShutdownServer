import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm") version "1.2.61"
    id("org.jetbrains.dokka") version "0.9.17"
}

version = "1.1.0"

application {
    mainClassName = "net.bjoernpetersen.shutdown.Main"
}

dependencies {
    // Basics
    implementation(kotlin("stdlib-jdk8"))
    implementation(
        group = "io.github.microutils",
        name = "kotlin-logging",
        version = Version.KOTLIN_LOGGING)

    // Config
    implementation(group = "com.jdiazcano.cfg4k", name = "cfg4k-core", version = Version.CFG4K)
    implementation(group = "com.jdiazcano.cfg4k", name = "cfg4k-yaml", version = Version.CFG4K)

    implementation(group = "io.vertx", name = "vertx-web", version = Version.VERTX)
    implementation(group = "io.vertx", name = "vertx-lang-kotlin", version = Version.VERTX) {
        exclude(group = "org.jetbrains.kotlin")
    }

    testRuntime(
        group = "org.junit.jupiter",
        name = "junit-jupiter-engine",
        version = Version.JUNIT)
    testRuntime(group = "org.slf4j", name = "slf4j-simple", version = Version.SLF4J)
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
