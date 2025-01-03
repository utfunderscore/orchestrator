plugins {
    id("java-library")
    kotlin("jvm") version "2.0.0"
    id("maven-publish")
}

val hermesVersion: String by project

dependencies {
    testImplementation(kotlin("test"))

    val hermesVersion: String by rootProject.extra
    api("io.github.utfunderscore.hermes:hermes.core:$hermesVersion")
    api("io.github.utfunderscore.hermes:hermes.netty:$hermesVersion")
    api("io.github.utfunderscore.hermes:hermes.kryo:$hermesVersion")

    val nettyVersion: String by rootProject.extra
    api("io.netty:netty-all:$nettyVersion")

    implementation("org.jetbrains:annotations:26.0.1")

    // Logging
    val log4jVersion: String by rootProject.extra
    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:$log4jVersion")

    api(project(":Shared"))
}

java {
    withSourcesJar()
}
