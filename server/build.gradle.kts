import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.util.Properties

plugins {
    kotlin("jvm") version "2.1.0"
    // shadow
    id("com.gradleup.shadow") version "9.0.0-beta4"
}

repositories {
    mavenCentral()
    mavenLocal()
}

var hermesVersion: String by rootProject.extra

dependencies {
    testImplementation(kotlin("test"))

    implementation("org.slf4j:slf4j-api:2.0.16")

    implementation("io.javalin:javalin:6.4.0")
    implementation("io.github.utfunderscore.hermes:hermes.core:$hermesVersion")
    implementation("io.github.utfunderscore.hermes:hermes.netty:$hermesVersion")
    implementation("io.github.utfunderscore.hermes:hermes.kryo:$hermesVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.2")
    implementation(project(":common"))
    implementation("com.github.docker-java:docker-java:3.4.1")
    implementation("com.github.docker-java:docker-java-transport-zerodep:3.4.1")
    implementation("io.github.revxrsal:lamp.common:4.0.0-rc.2")
    implementation("io.github.revxrsal:lamp.cli:4.0.0-rc.2")
    implementation("io.netty:netty-all:4.2.0.RC1")
    implementation("com.esotericsoftware:kryo:5.6.2")

    implementation("org.jetbrains.exposed:exposed:0.17.14")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.57.0")
    implementation("com.impossibl.pgjdbc-ng:pgjdbc-ng-all:0.8.5")

    implementation("org.apache.logging.log4j:log4j-api:2.20.0")
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.20.0")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "org.readutf.orchestrator.server.ServerStarterKt"
    }
}

tasks.getByName<ShadowJar>("shadowJar") {
    doLast {
        outputs.files.forEach { file ->
            val output = projectDir.resolve("docker").resolve(file.name)
            if (output.exists()) output.delete()
            file.copyTo(output, overwrite = true)
        }
    }
}
tasks.register("runDevContainer") {
    dependsOn("shadowJar", "createProperties")
    doLast {
        exec {
            commandLine(
                "sh",
                "-c",
                """
                docker ps -a --filter "ancestor=orchestrator-dev-server" --format "{{.ID}}" | xargs -r docker rm -f && \
                docker build -t orchestrator-dev-server docker && \
                docker run --name aOrchestrator -p 2323:2323 -p 9191:9191 -v /var/run/docker.sock:/var/run/docker.sock -it --hostname=orchestrator --network=orchestrator -d orchestrator-dev-server
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

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
