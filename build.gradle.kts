plugins {
    kotlin("jvm") version "2.0.0"
}

group = "org.readutf.orchestrator"
version = "1.3.1"

dependencies {
    testImplementation(kotlin("test"))
}

repositories {
    maven {
        name = "utfunderscoreReleases"
        url = uri("https://reposilite.readutf.org/releases")
    }
}

extra["hermesVersion"] = "1.5.2-SNAPSHOT-1722272314060"
extra["nettyVersion"] = "4.1.111.Final"
extra["log4jVersion"] = "2.23.1"

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
