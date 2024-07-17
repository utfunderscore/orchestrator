plugins {
    kotlin("jvm")
    id("java-library")
}

group = "org.readutf.orchestrator"
version = "1.0-SNAPSHOT"

dependencies {
    // add fastjson2
    implementation("com.alibaba:fastjson:2.0.51")

    api("org.panda-lang:expressible:1.3.6") // Core library
    api("org.panda-lang:expressible-kt:1.3.6") // Kotlin extensions

    implementation("io.netty:netty-all:4.1.111.Final")

    api("io.github.oshai:kotlin-logging-jvm:5.1.0")
    api("com.esotericsoftware:kryo5:5.6.0")

    compileOnly("org.readutf.hermes:core:1.3.7")

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
