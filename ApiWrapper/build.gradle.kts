plugins {
    kotlin("jvm")
    id("java-library")
    id("maven-publish")
}

group = "org.readutf.orchestrator"

dependencies {

    testImplementation(kotlin("test"))

    api(project(":Shared"))

    api("org.java-websocket:Java-WebSocket:1.5.7")

    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC.2")

    api("com.squareup.retrofit2:retrofit:2.11.0")
    api("com.squareup.okhttp3:logging-interceptor:4.11.0")
    api("com.squareup.retrofit2:converter-jackson:2.11.0")

    val log4jVersion: String by rootProject.extra
    testImplementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    testImplementation("org.apache.logging.log4j:log4j-slf4j2-impl:$log4jVersion")
}

publishing {
    repositories {
        maven {
            name = "utfunderscore"
            url = uri("https://repo.readutf.org/releases")
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            groupId = "org.readutf.orchestrator"
            artifactId = "api-wrapper"
            version = rootProject.version.toString()

            from(components["java"])
        }
    }
}

java {
    withSourcesJar()
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
