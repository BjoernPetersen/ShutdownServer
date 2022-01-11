@file:Suppress("UnstableApiUsage")

import com.diffplug.spotless.LineEnding
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm") version libs.versions.kotlin
    kotlin("kapt") version libs.versions.kotlin
    alias(libs.plugins.dokka)
    alias(libs.plugins.spotless)
    alias(libs.plugins.shadow)

    idea
}

version = "7.0.0-SNAPSHOT"

application {
    mainClassName = "net.bjoernpetersen.shutdown.Main"
}

dependencies {
    // Basics
    implementation(kotlin("stdlib-jdk8", version = libs.versions.kotlin.get()))
    implementation(libs.kotlin.logging)
    implementation(libs.slf4j.api)
    implementation(libs.logback)

    // Working around kapt bug https://youtrack.jetbrains.com/issue/KT-35721
    compileOnly("org.jetbrains:annotations:19.0.0")

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
            jvmTarget = "1.8"
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

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
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
