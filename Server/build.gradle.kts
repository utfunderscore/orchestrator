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

tasks.jar {
    manifest {
        attributes["Main-Class"] = "org.readutf.orchestrator.server.ServerStarterKt"
    }
    finalizedBy("shadowJar")
}

tasks.register("generateDockerFile") {

    val buildFile = file("/build/docker/")
    buildFile.mkdirs()

    File(buildFile, "Dockerfile").writeText(
        """
        FROM eclipse-temurin:21-jdk-jammy as deps
        
        WORKDIR /orchestrator

        ADD https://github.com/utfunderscore/orchestrator/releases/download/latest/Server-$version-all.jar /orchestrator

        EXPOSE 2980
        EXPOSE 9393

        CMD ["java", "-jar", "Server-$version-all.jar"]
        """.trimIndent(),
    )

//    commandLine("docker", "build", "-t", "utfunderscore/orchestrator:$version", buildFile.absolutePath)
}

tasks.register<Exec>("generateDevDockerFile") {

    val devFolder = file("/build/docker/dev/")
    devFolder.mkdirs()

    val targetFile = devFolder.resolve("Server-$version-all.jar")
    if (!targetFile.exists()) {
        file("/build/libs/Server-$version-all.jar").copyTo(targetFile)
    }

    File(devFolder, "Dockerfile").writeText(
        """
        FROM eclipse-temurin:21-jdk-jammy as deps
        
        WORKDIR /orchestrator

        ADD /Server-$version-all.jar /orchestrator
        ADD /settings.yml /orchestrator

        EXPOSE 2980
        EXPOSE 9393

        CMD ["java", "-jar", "Server-$version-all.jar"]
        """.trimIndent(),
    )

    commandLine("docker", "build", "-t", "utfunderscore/orchestrator-dev:$version", devFolder.absolutePath)
}

tasks.register("createProperties") {
    dependsOn(tasks.processResources)
    doLast {
        val propertiesFile = file("$buildDir/resources/main/version.properties")
        propertiesFile.parentFile.mkdirs()
        propertiesFile.writer().use { writer ->
            val properties = Properties()
            properties["version"] = project.version.toString()
            properties.store(writer, null)
        }
    }
}

tasks.named("classes") {
    dependsOn("createProperties")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
