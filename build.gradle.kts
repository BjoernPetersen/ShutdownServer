import com.diffplug.spotless.LineEnding
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.github.ben-manes.versions") version "0.39.0"
    id("com.diffplug.spotless") version "5.12.5"

    application
    kotlin("jvm") version "1.5.10"
    kotlin("kapt") version "1.5.10"
    id("com.github.johnrengelman.shadow") version "7.0.0"

    idea
}

version = "7.0.0-SNAPSHOT"


application {
    mainClass.set("net.bjoernpetersen.shutdown.Main")
}

dependencies {
    // Basics
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    implementation(libs.logging.slf4j.api)
    implementation(libs.logging.kotlin)
    implementation(libs.logging.logback)

    // Working around kapt bug https://youtrack.jetbrains.com/issue/KT-35721
    compileOnly("org.jetbrains:annotations:19.0.0")

    // CLI args
    implementation(libs.clikt)

    // Config
    implementation(libs.cfg4k.core)
    implementation(libs.cfg4k.yaml)

    implementation(libs.jackson.databind)
    implementation(libs.jackson.kotlin)
    implementation(libs.jackson.yaml)

    implementation(libs.antlr)

    // Vertx
    implementation(libs.vertx.web)
    implementation(libs.vertx.kotlin) {
        exclude(group = "org.jetbrains.kotlin")
    }

    // Dependency injection
    implementation(libs.dagger.lib)
    kapt(libs.dagger.compiler)

    testRuntimeOnly(libs.junit.engine)
    testImplementation(libs.junit.api)
    testImplementation(libs.junit.vertx)
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    withType<Test> {
        useJUnitPlatform()
    }

    withType<Jar> {
        from(project.projectDir) {
            include("LICENSE")
        }
    }

    "processResources"(ProcessResources::class) {
        filesMatching("**/version.properties") {
            filter {
                it.replace("%APP_VERSION%", version.toString())
            }
        }
    }

    dependencyUpdates {
        rejectVersionIf {
            val version = candidate.version
            isUnstable(version, currentVersion) || isWrongPlatform(version, currentVersion)
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

repositories {
    mavenCentral()
    jcenter()
}

idea {
    module {
        isDownloadJavadoc = true
    }
}

spotless {
    kotlin {
        ktlint()
        lineEndings = LineEnding.UNIX
        endWithNewline()
    }
    kotlinGradle {
        ktlint()
        lineEndings = LineEnding.UNIX
        endWithNewline()
    }
    format("markdown") {
        target("**/*.md")
        lineEndings = LineEnding.UNIX
        endWithNewline()
    }
}


fun isUnstable(version: String, currentVersion: String): Boolean {
    val lowerVersion = version.toLowerCase()
    val lowerCurrentVersion = currentVersion.toLowerCase()
    return listOf(
        "alpha",
        "beta",
        "rc",
        "m",
        "eap",
        "cr",
    ).any { it in lowerVersion && it !in lowerCurrentVersion }
}

fun isWrongPlatform(version: String, currentVersion: String): Boolean {
    return "android" in currentVersion && "android" !in version
}
