import java.util.Properties

plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "org.readutf.orchestrator"

dependencies {
    testImplementation(kotlin("test"))

    implementation(project(":Shared"))

    // Javalin
    implementation("io.javalin:javalin:+")
    implementation("io.javalin.community.routing:routing-core:6.1.6")
    implementation("io.javalin.community.routing:routing-annotated:6.1.6")

    val nettyVersion: String by rootProject.extra
    implementation("io.netty:netty-all:$nettyVersion")

    val hermesVersion: String by rootProject.extra
    implementation("org.readutf.hermes:core:$hermesVersion")
    implementation("org.readutf.hermes:netty:$hermesVersion")
    implementation("org.readutf.hermes:kryo:$hermesVersion")
    implementation("org.readutf.hermes:fastjson:$hermesVersion")

    // Kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.52")

    // Logging
    val log4jVersion: String by rootProject.extra
    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:$log4jVersion")

    // Commands
    implementation("com.github.Revxrsal.Lamp:common:3.2.1")
    implementation("com.github.Revxrsal.Lamp:cli:3.2.1")

    val exposed = "0.51.0"
    implementation("org.jetbrains.exposed:exposed-core:$exposed")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposed")

    // Docker
    implementation("com.github.docker-java:docker-java-core:3.4.0")
    implementation("com.github.docker-java:docker-java-transport-zerodep:3.4.0")

    // Hoplite
    implementation("com.sksamuel.hoplite:hoplite-core:2.8.0.RC3")
    runtimeOnly("com.sksamuel.hoplite:hoplite-yaml:2.8.0.RC3")

    implementation("com.h2database:h2:2.2.224")
}

tasks {

    jar {
        manifest {
            attributes["Main-Class"] = "org.readutf.orchestrator.server.ServerStarterKt"
        }
        finalizedBy("createProperties")
    }

    shadowJar {
        archiveFileName = "Orchestrator.jar"
        finalizedBy("copyArchive")
    }

    register("copyArchive") {
        doLast {
            val archive = file("$buildDir/libs/Orchestrator.jar")
            val destination = file("$projectDir/docker/Orchestrator.jar")
            archive.copyTo(destination, true)
        }
    }

    register("createProperties") {
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
    named("classes") {
        dependsOn("createProperties")
    }

    test {
        useJUnitPlatform()
    }
}
kotlin {
    jvmToolchain(17)
}
