plugins {
    kotlin("jvm") version "2.1.0"
    `java-library`
}

repositories {
    mavenCentral()
}

val hermesVersion: String by rootProject.extra

dependencies {
    testImplementation(kotlin("test"))

    api("org.slf4j:slf4j-api:2.0.0-alpha1")
    api("io.github.oshai:kotlin-logging-jvm:7.0.0")
    api("io.github.utfunderscore.hermes:hermes.core:$hermesVersion")
    api("io.github.utfunderscore.hermes:hermes.netty:$hermesVersion")
    api("io.github.utfunderscore.hermes:hermes.kryo:$hermesVersion")
    api("com.esotericsoftware:kryo:5.6.2")
    api("org.jetbrains:annotations:26.0.1")
    api("io.netty:netty-all:4.2.0.RC1")
    api(project(":common"))

    implementation("org.apache.logging.log4j:log4j-api:2.20.0")
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.20.0")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "org.readutf.orchestrator.client.TestClientKt"
    }
}

tasks.register("copyArchive") {
    doLast {
        val archive = file("$buildDir/libs/orchestrator-client-all.jar")
        val destination = file("$projectDir/build/docker/demo/orchestrator-client-all.jar")
        archive.copyTo(destination, true)
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
                docker build -t orchestrator-dev-client docker
                """.trimIndent(),
            )
        }
    }
}

tasks.register("runDevContainer") {
    dependsOn("buildDevContainer")

    doLast {
        exec {
            commandLine(
                "sh",
                "-c",
                """
                docker ps -a --filter "ancestor=orchestrator-dev-client" --format "{{.ID}}" | xargs -r docker rm -f && \
                docker run -p 25565:25565 -v /var/run/docker.sock:/var/run/docker.sock --network=orchestrator -d orchestrator-dev-client
                """.trimIndent(),
            )
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
