plugins {
    `java-library`
    kotlin("jvm") version "2.1.0"
}

group = "org.readutf.orchestrator"
version = "unspecified"

repositories {
    mavenCentral()
}

val hermesVersion = rootProject.extra["hermesVersion"] as String

dependencies {
    testImplementation(kotlin("test"))

    api("com.michael-bull.kotlin-result:kotlin-result:2.0.1")

    compileOnly("io.javalin:javalin:6.4.0")

    compileOnly("io.github.utfunderscore.hermes:hermes.core:$hermesVersion")
    compileOnly("io.github.utfunderscore.hermes:hermes.netty:$hermesVersion")
    compileOnly("io.github.utfunderscore.hermes:hermes.kryo:$hermesVersion")
    compileOnly("com.esotericsoftware:kryo:5.6.2")

    compileOnly("com.fasterxml.jackson.core:jackson-databind:2.18.2")
    compileOnly("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.2")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
