plugins {
    kotlin("jvm") version "2.0.0"
}

group = "org.readutf.orchestrator"
version = "1.7.3"

dependencies {
    testImplementation(kotlin("test"))
}

repositories {
    mavenLocal()
    maven {
        name = "utfunderscoreReleases"
        url = uri("https://reposilite.readutf.org/releases")
    }
}

extra["hermesVersion"] = "1.6.3"
extra["nettyVersion"] = "4.1.111.Final"
extra["log4jVersion"] = "2.23.1"

subprojects {
    version = rootProject.version

    repositories {
        mavenLocal()
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
