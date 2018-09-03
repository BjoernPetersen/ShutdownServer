import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm") version "1.2.61"
    id("org.jetbrains.dokka") version "0.9.17"
}

version = "1.0.0"

application {
    mainClassName = "net.bjoernpetersen.shutdown.Main"
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile("org.slf4j:slf4j-simple:1.7.25")
    compile("com.google.guava:guava:26.0-jre")

    compile("io.github.cdimascio:java-dotenv:3.1.2")

    compile("io.vertx:vertx-web:3.5.3")
    compile("io.vertx:vertx-lang-kotlin:3.5.3")

    testCompile(group = "org.junit.jupiter", name = "junit-jupiter-api", version = "5.2.0")
    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-engine",
        version = "5.2.0")
    testCompile(kotlin("test-junit", "1.2.61"))
    testCompile("io.vertx:vertx-junit5:3.5.3")
}

buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath("io.github.cdimascio:java-dotenv:3.1.2")
    }
}

val compileKotlin by tasks.getting(KotlinCompile::class) {
    kotlinOptions.jvmTarget = "1.8"
}
val compileTestKotlin by tasks.getting(KotlinCompile::class) {
    kotlinOptions.jvmTarget = "1.8"
}

val test by tasks.getting(org.gradle.api.tasks.testing.Test::class) {
    useJUnitPlatform()
}

val dokka by tasks.getting(org.jetbrains.dokka.gradle.DokkaTask::class) {
    // TODO maybe switch to javadoc (or another) format
    outputFormat = "html"
    outputDirectory = "$buildDir/javadoc"
}

val jar by tasks.getting(Jar::class) {
    java {
        sourceSets["main"].resources.exclude("**/.env")
    }
}

repositories {
    jcenter()
}
