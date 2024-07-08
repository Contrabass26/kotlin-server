plugins {
    kotlin("jvm") version "2.0.0"
    application
}

group = "me.jsedwards"
version = "1.0-SNAPSHOT"

application {
    mainClass = "Main"
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    testImplementation(kotlin("test"))

    // Kotlin coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.9.0-RC")

    // GUI appearance
    implementation("com.formdev:flatlaf:3.2.1")
    implementation("com.github.Dansoftowner:jSystemThemeDetector:3.8")

    // JSON parsing
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.3")
    implementation("com.fasterxml.jackson.core:jackson-core:2.15.3")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.15.3")

    // Apache commons lang
    implementation("org.apache.commons:commons-lang3:3.13.0")

    // Apache commons IO
    implementation("commons-io:commons-io:2.15.1")

    // Jsoup - HTML parsing
    implementation("org.jsoup:jsoup:1.16.1")

    // Log4j
    implementation("org.apache.logging.log4j:log4j-api:2.21.1")
    implementation("org.apache.logging.log4j:log4j-core:2.21.1")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}