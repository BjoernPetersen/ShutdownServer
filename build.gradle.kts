@file:Suppress("UnstableApiUsage")

import com.diffplug.spotless.LineEnding
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm") version libs.versions.kotlin
    kotlin("kapt") version libs.versions.kotlin
    alias(libs.plugins.spotless)
    alias(libs.plugins.shadow)

    idea
}

version = "7.0.0-SNAPSHOT"

application {
    mainClass.set("net.bjoernpetersen.shutdown.Main")
}

dependencies {
    // Basics
    implementation(kotlin("stdlib-jdk8", version = libs.versions.kotlin.get()))
    implementation(libs.kotlin.logging)
    implementation(libs.slf4j.api)
    implementation(libs.logback)

    // Working around kapt bug https://youtrack.jetbrains.com/issue/KT-35721
    compileOnly("org.jetbrains:annotations:23.0.0")

    // CLI args
    implementation(libs.clikt)

    // Config
    implementation(libs.bundles.cfg4k)
    implementation(kotlin("reflect", version = libs.versions.kotlin.get()))
    implementation(libs.bundles.jackson)
    implementation(libs.stringtemplate)

    // Vertx
    implementation(libs.vertx.web)
    implementation(libs.vertx.kotlin) {
        exclude(group = "org.jetbrains.kotlin")
    }

    // Dependency injection
    implementation(libs.dagger.runtime)
    kapt(libs.dagger.compiler)

    testRuntimeOnly(libs.junit.engine)
    testImplementation(libs.junit.api)
    testImplementation(libs.vertx.junit)
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
        }
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
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
    // TODO: required for cfg4k
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
