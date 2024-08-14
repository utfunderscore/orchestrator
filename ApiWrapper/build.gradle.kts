plugins {
    kotlin("jvm")
    id("maven-publish")
}

group = "org.readutf.orchestrator"

dependencies {

    testImplementation(kotlin("test"))

    implementation(project(":Shared"))

    implementation("com.alibaba.fastjson2:fastjson2:2.0.52")
    implementation("org.java-websocket:Java-WebSocket:1.5.7")
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
            artifactId = "api-wrapper"
            version = "1.0.0"

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
