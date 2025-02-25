import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    kotlin("jvm") version "2.1.0"
    `maven-publish`
}

group = "org.readutf.orchestrator"
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

    apply(plugin = "kotlin")
    apply(plugin = "maven-publish")

    kotlin {
        jvmToolchain(17)
    }

    tasks.withType<JavaCompile> {
        options.compilerArgs.add("-parameters")
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                groupId = rootProject.group.toString()
                version = rootProject.version.toString()
                artifactId = project.name

                from(components["java"])
            }
        }
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
