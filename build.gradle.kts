plugins {
    kotlin("jvm") version "2.0.0"
}

group = "org.readutf.orchestrator"
version = "1.0.0"

dependencies {
    testImplementation(kotlin("test"))
}

repositories {
    maven {
        name = "utfunderscoreReleases"
        url = uri("https://reposilite.readutf.org/releases")
    }
    maven {
        name = "utfunderscore"
        url = uri("https://reposilite.readutf.org/snapshots")
        credentials(PasswordCredentials::class)
        authentication {
            create<BasicAuthentication>("basic")
        }
    }
}

subprojects {
    version = rootProject.version
    repositories {
        maven {
            name = "utfunderscore"
            url = uri("https://reposilite.readutf.org/snapshots")
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }

            maven {
                name = "utfunderscoreReleases"
                url = uri("https://reposilite.readutf.org/releases")
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
