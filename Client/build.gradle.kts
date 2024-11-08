plugins {
    id("java-library")
    kotlin("jvm") version "2.0.0"
    id("maven-publish")
}

group = "org.readutf.orchestrator"

repositories {
}

val hermesVersion: String by project

dependencies {
    testImplementation(kotlin("test"))

    val hermesVersion: String by rootProject.extra
    api("org.readutf.hermes:core:$hermesVersion")
    api("org.readutf.hermes:netty:$hermesVersion")
    api("org.readutf.hermes:kryo:$hermesVersion")

    val nettyVersion: String by rootProject.extra
    api("io.netty:netty-all:$nettyVersion")

    implementation("org.jetbrains:annotations:26.0.1")

    // Logging
    val log4jVersion: String by rootProject.extra
    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:$log4jVersion")

    api(project(":Shared"))
}

java {
    withSourcesJar()
}

publishing {
    repositories {
        maven {
            name = "utfunderscore"
            url = uri("https://reposilite.readutf.org/releases")
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            groupId = "org.readutf.orchestrator"
            artifactId = "client"
            version = rootProject.version.toString()

            from(components["java"])
        }
    }
}
tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
