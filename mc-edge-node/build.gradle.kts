import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.util.*

plugins {
    kotlin("jvm") version "2.1.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    implementation(project(":api"))
    implementation(project(":client"))

    implementation("net.minestom:minestom-snapshots:dev")
    implementation("net.minestom:scratch:dev")

    implementation("io.github.oshai:kotlin-logging-jvm:7.0.0")

    implementation("org.apache.logging.log4j:log4j-api:2.20.0")
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.20.0")

    // Gson
    implementation("com.google.code.gson:gson:2.8.8")
}

tasks.getByName<ShadowJar>("shadowJar") {
    dependsOn("createProperties")
    doLast {
        outputs.files.forEach { file ->
            val output = projectDir.resolve("docker").resolve(file.name)
            if (output.exists()) output.delete()
            file.copyTo(output, overwrite = true)
        }
    }
}

tasks.register("buildDevContainer") {
    dependsOn("shadowJar")

    doLast {
        exec {
            commandLine(
                "sh",
                "-c",
                """
                docker build -t orchestrator-edge-node docker
                """.trimIndent(),
            )
        }
    }
}

tasks.register("createProperties") {
    doLast {
        val propertiesFile = file("$buildDir/resources/main/version.properties")
        propertiesFile.parentFile.mkdirs()
        propertiesFile.writer().use { writer ->
            val properties = Properties()
            properties["version"] = project.version.toString()
            properties["buildTime"] = System.currentTimeMillis().toString()
            properties.store(writer, null)
        }
    }
}

kotlin {
    jvmToolchain(23)
    kotlin
}

// set mainclass manifest

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "org.readutf.orchestrator.edge.EdgeNodeKt"
    }
}
