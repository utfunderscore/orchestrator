import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.util.Properties

plugins {
    kotlin("jvm") version "2.1.0"
    id("com.gradleup.shadow") version "9.0.0-beta4"
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.xerial:sqlite-jdbc:3.49.1.0")
    testImplementation(libs.tinylog.api)
    testImplementation(libs.tinylog.slf4j)
    testImplementation(libs.tinylog.impl)

    implementation("org.slf4j:slf4j-api:2.0.16")

    implementation(libs.javalin)
    implementation(libs.hermes.core)
    implementation(libs.hermes.kryo)
    implementation(libs.hermes.netty)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.module.kotlin)
    implementation(project(":common"))
    implementation(libs.docker)
    implementation(libs.docker.transport)
    implementation(libs.lamp.common)
    implementation(libs.lamp.cli)
    implementation(libs.netty.all)
    implementation(libs.kryo)

    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.postgresql)

    implementation(libs.tinylog.api)
    implementation(libs.tinylog.slf4j)
    implementation(libs.tinylog.impl)
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "org.readutf.orchestrator.server.ServerStarterKt"
    }
}

tasks.getByName<ShadowJar>("shadowJar") {
    dependsOn("createProperties")
    archiveFileName = "orchestrator.jar"
    doLast {
        outputs.files.forEach { file ->
            val output = projectDir.resolve("docker").resolve(file.name)
            if (output.exists()) output.delete()
            file.copyTo(output, overwrite = true)
        }
    }
}
tasks.register("startDevEnvironment") {

    exec {
        val result = commandLine(
            "sh",
            "-c",
            """
                docker build -t orchestrator-dev-server docker && \
                docker compose -f docker/docker-compose.yml stop && \
                docker compose -f docker/docker-compose.yml rm -f && \
                docker compose -f docker/docker-compose.yml up -d
            """.trimIndent(),
        )
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

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
