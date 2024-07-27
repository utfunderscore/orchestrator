plugins {
    kotlin("jvm")
}

group = "org.readutf.orchestrator"
version = "1.0-SNAPSHOT"

dependencies {
    testImplementation(kotlin("test"))

    implementation(project(":Shared"))

    // Javalin
    implementation("io.javalin:javalin:+")
    implementation("io.javalin.community.routing:routing-core:6.1.6")
    implementation("io.javalin.community.routing:routing-annotated:6.1.6")

    implementation("io.netty:netty-all:4.1.111.Final")

    implementation("org.readutf.hermes:core:1.5.1")
    implementation("org.readutf.hermes:netty:1.5.1")
    implementation("org.readutf.hermes:kryo:1.5.1")

    // Hoplite
    implementation("com.sksamuel.hoplite:hoplite-core:2.7.5")
    implementation("com.sksamuel.hoplite:hoplite-yaml:2.8.0.RC3")

    // Kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.52")

    // Logging
    implementation("org.apache.logging.log4j:log4j-api:2.14.1")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.23.1")

    // FastJson2
    implementation("com.alibaba:fastjson:+")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
