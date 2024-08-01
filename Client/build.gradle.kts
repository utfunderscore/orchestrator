plugins {
    kotlin("jvm") version "2.0.0"
}

group = "org.readutf.orchestrator"

repositories {
}

val hermesVersion: String by project

dependencies {
    testImplementation(kotlin("test"))

    val hermesVersion: String by rootProject.extra
    implementation("org.readutf.hermes:core:$hermesVersion")
    implementation("org.readutf.hermes:netty:$hermesVersion")
    implementation("org.readutf.hermes:kryo:$hermesVersion")

    val nettyVersion: String by rootProject.extra
    implementation("io.netty:netty-all:$nettyVersion")

    // Logging
    val log4jVersion: String by rootProject.extra
    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:$log4jVersion")

    // FastJSON2
    implementation("com.alibaba:fastjson:+")

    implementation(project(":Shared"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
