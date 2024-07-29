plugins {
    kotlin("jvm")
    id("java-library")
}

group = "org.readutf.orchestrator"
version = "1.0-SNAPSHOT"

dependencies {
    // add fastjson2
    implementation("com.alibaba:fastjson:2.0.51")

    val nettyVersion: String by rootProject.extra
    implementation("io.netty:netty-all:$nettyVersion")

    api("io.github.oshai:kotlin-logging-jvm:5.1.0")
    api("com.esotericsoftware:kryo5:5.6.0")

    val hermesVersion: String by rootProject.extra
    compileOnly("org.readutf.hermes:core:$hermesVersion")

    implementation("io.github.classgraph:classgraph:4.8.174")

    implementation("org.jetbrains:annotations:24.1.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.0.0")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
