plugins {
    `java-library`
    kotlin("jvm") version "2.1.0"
}

repositories {
    mavenCentral()
}

val hermesVersion = rootProject.extra["hermesVersion"] as String

dependencies {
    testImplementation(kotlin("test"))

    api("com.michael-bull.kotlin-result:kotlin-result:2.0.1")

    compileOnly("io.javalin:javalin:6.4.0")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.0")

    compileOnly("org.readutf.hermes:core:$hermesVersion")
    compileOnly("org.readutf.hermes:netty:$hermesVersion")
    compileOnly("org.readutf.hermes:kryo:$hermesVersion")
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
