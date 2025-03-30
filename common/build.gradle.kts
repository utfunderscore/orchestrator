plugins {
    `java-library`
    kotlin("jvm") version "2.1.0"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    api(libs.result)

    compileOnly(libs.hermes.core)
    compileOnly(libs.hermes.netty)
    compileOnly(libs.hermes.kryo)
    compileOnly(libs.kryo)

    compileOnly(libs.jackson.module.kotlin)
    compileOnly(libs.jackson.databind)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
