import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.util.Properties

plugins {
    id("com.gradleup.shadow") version "9.0.0-beta4"
    kotlin("jvm") version "2.1.0"
    kotlin("kapt")
}

repositories {
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    testImplementation(kotlin("test"))

    implementation(project(":common"))
    implementation(project(":client"))
    implementation(project(":api"))

    compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    kapt("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
}

tasks.register("copyArchive") {
    doLast {
        val archive = file("$buildDir/libs/orchestrator-client-all.jar")
        val destination = file("$projectDir/build/docker/demo/orchestrator-client-all.jar")
        archive.copyTo(destination, true)
    }
}

tasks.getByName<ShadowJar>("shadowJar") {
    archiveFileName.set("proxy.jar")
    doLast {
        outputs.files.forEach { file ->
            val output = projectDir.resolve("docker").resolve(file.name)
            if (output.exists()) output.delete()
            file.copyTo(output, overwrite = true)
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

tasks.register("buildDevContainer") {
    dependsOn("shadowJar")

    doLast {
        exec {
            commandLine(
                "sh",
                "-c",
                """
                docker build -t orchestrator-proxy docker
                """.trimIndent(),
            )
        }
    }
}
tasks.test {
    useJUnitPlatform()
}
