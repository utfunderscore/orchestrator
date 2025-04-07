plugins {
    kotlin("jvm")
    id("com.gradleup.shadow") version "9.0.0-beta11"
}

group = "org.readutf.orchestrator"
version = "2.1.0"

repositories {
    mavenCentral()
    maven("https://repo.panda-lang.org/releases")
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("net.dv8tion:JDA:5.3.1")
    implementation("club.minnced:jda-ktx:0.12.0")

    implementation(project(":common"))
    implementation(project(":api"))

    implementation("dev.rollczi:litecommands-jda:3.9.7")

    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.postgresql)

    implementation(libs.tinylog.api)
    implementation(libs.tinylog.impl)
    implementation(libs.tinylog.slf4j)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(23)
}
