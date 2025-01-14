plugins {
    kotlin("jvm") version "2.1.0"
}

group = "io.github.utfunderscore"
version = "2.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    api("com.squareup.retrofit2:retrofit:2.11.0")
    api("org.java-websocket:Java-WebSocket:1.6.0")
    api("io.github.oshai:kotlin-logging-jvm:7.0.0")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.2")

    api(project(":common"))
}

tasks.test {
    useJUnitPlatform()
}
