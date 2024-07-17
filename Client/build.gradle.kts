plugins {
    kotlin("jvm") version "2.0.0"
}

group = "org.readutf.orchestrator"
version = "1.0-SNAPSHOT"

repositories {
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("org.readutf.hermes:core:1.3.7")
    implementation("org.readutf.hermes:netty:1.3.7")
    implementation("org.readutf.hermes:kryo:1.3.7")

    implementation("io.netty:netty-all:4.1.111.Final")

    // Logging
    implementation("org.apache.logging.log4j:log4j-api:2.14.1")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.23.1")

    implementation(project(":Shared"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
