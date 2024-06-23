plugins {
    kotlin("jvm") version "2.0.0"
}

group = "me.jsedwards"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io") // For jSystemThemeDetector
}

dependencies {
    testImplementation(kotlin("test"))

    // Kotlin coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC")

    // GUI appearance
    implementation("com.formdev:flatlaf:3.2.1")
    implementation("com.github.Dansoftowner:jSystemThemeDetector:3.8")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}