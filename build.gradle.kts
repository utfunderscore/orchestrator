import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    kotlin("jvm") version "2.1.0"
}

group = "io.github.utfunderscore"
version = "2.0.0"

dependencies {
    testImplementation(kotlin("test"))
}

repositories {
    mavenCentral()
    mavenLocal()
}

extra["hermesVersion"] = "1.0.2"

subprojects {

    group = rootProject.group
    version = rootProject.version

    apply(plugin = "java")
    apply(plugin = "kotlin")

    kotlin {
        jvmToolchain(17)
    }

    tasks.withType<JavaCompile> {
        options.compilerArgs.add("-parameters")
    }

    tasks.withType<KotlinJvmCompile> {
        compilerOptions {
            javaParameters = true
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
