plugins {
    kotlin("jvm") version "2.1.0"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    api("com.squareup.retrofit2:retrofit:2.11.0")
    api("com.squareup.retrofit2:converter-jackson:2.11.0")
    api("org.java-websocket:Java-WebSocket:1.6.0")
    api("io.github.oshai:kotlin-logging-jvm:7.0.0")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")

    implementation(libs.jackson.databind)
    implementation(libs.jackson.module.kotlin)

    api(project(":common"))
}

tasks.test {
    useJUnitPlatform()
}
