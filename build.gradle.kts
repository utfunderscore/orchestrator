plugins {
    kotlin("jvm") version "2.0.0"
}

group = "org.readutf.orchestrator"
version = "1.0-SNAPSHOT"

dependencies {
    testImplementation(kotlin("test"))
}

repositories {
    maven {
        url = uri("https://reposilite.readutf.org/releases")
    }
}

subprojects {
    repositories {
        maven {
            url = uri("https://reposilite.readutf.org/releases")
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
