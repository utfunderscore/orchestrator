plugins {
    kotlin("jvm") version "2.0.0"
}

group = "org.readutf.orchestrator"
version = "1.9.0"

dependencies {
    testImplementation(kotlin("test"))
}

repositories {
    mavenCentral()
}

repositories {
    mavenLocal()
    maven {
        name = "utfunderscoreReleases"
        url = uri("https://repo.readutf.org/releases")
    }
}

extra["hermesVersion"] = "1.0.1"
extra["nettyVersion"] = "4.1.111.Final"
extra["log4jVersion"] = "2.23.1"

subprojects {
    version = rootProject.version

    apply(plugin = "java")
    apply(plugin = "kotlin")

    group = rootProject.group
    version = rootProject.version

    java {
        withSourcesJar()
    }

    tasks.test {
        useJUnitPlatform()
    }
    kotlin {
        jvmToolchain(17)
    }
    repositories {
        mavenCentral()
    }
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
