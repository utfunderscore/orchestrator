plugins {
    kotlin("jvm")
    id("java-library")
    id("maven-publish")
}

group = "org.readutf.orchestrator"

dependencies {

    val nettyVersion: String by rootProject.extra
    implementation("io.netty:netty-all:$nettyVersion")

    api("io.github.oshai:kotlin-logging-jvm:5.1.0")
    api("com.esotericsoftware:kryo5:5.6.0")
    api("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    api("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")

    val hermesVersion: String by rootProject.extra
    api("org.readutf.hermes:core:$hermesVersion")

    implementation("io.github.classgraph:classgraph:4.8.174")

    implementation("org.jetbrains:annotations:24.1.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.0.0")
}

publishing {
    repositories {
        maven {
            name = "utfunderscore"
            url = uri("https://reposilite.readutf.org/releases")
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            groupId = "org.readutf.orchestrator"
            artifactId = "shared"
            version = rootProject.version.toString()

            from(components["java"])
        }
    }
}

java {
    withSourcesJar()
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
