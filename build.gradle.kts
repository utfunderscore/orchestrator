import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    kotlin("jvm") version "2.1.0"
    `maven-publish`
    id("com.vanniktech.maven.publish") version "0.30.0"
    signing
}

group = "org.readutf.orchestrator"
version = "2.0.0"

dependencies {
    testImplementation(kotlin("test"))
}

repositories {
    mavenCentral()
    mavenLocal()
}

extra["hermesVersion"] = "1.0.2"

subprojects {

    group = rootProject.group
    version = rootProject.version

    apply(plugin = "kotlin")
    apply(plugin = "com.vanniktech.maven.publish")

    kotlin {
        jvmToolchain(17)
    }

    tasks.withType<JavaCompile> {
        options.compilerArgs.add("-parameters")
    }

    if (name == "common" || name == "api" || name == "client") {

        publishing {
            repositories {
                maven {
                    name = "utfRepoReleases"
                    // or when a separate snapshot repository is required
                    url = uri(if (version.toString().endsWith("SNAPSHOT")) "https://mvn.utf.lol/snapshots" else "https://mvn.utf.lol/releases")
                    credentials(PasswordCredentials::class)
                }
            }
        }

        mavenPublishing {

            coordinates(
                groupId = group.toString(),
                version = version.toString(),
                artifactId = name,
            )

            pom {
                name.set("Orchestrator")
                description.set("Orchestrator shared code")
                inceptionYear.set("2024")

                url.set("https://github.com/utfunderscore/orchestrator")
                licenses {
                    license {
                        name.set("GPLv3")
                        url.set("https://www.gnu.org/licenses/gpl-3.0.html")
                        distribution.set("https://www.gnu.org/licenses/gpl-3.0.html")
                    }
                }
                developers {
                    developer {
                        id.set("utfunderscore")
                        name.set("utfunderscore")
                        url.set("utf.lol")
                    }
                }
                scm {
                    url.set("https://github.com/utfunderscore/orchestrator/")
                    connection.set("scm:git:git://github.com/utfunderscore/Orchestrator.git")
                    developerConnection.set("scm:git:ssh://git@github.com/utfunderscore/Orchestrator.git")
                }
            }

            publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
            signAllPublications()
        }

        signing {
            sign(publishing.publications)
        }
    }

    tasks.withType<KotlinJvmCompile> {
        compilerOptions {
            javaParameters = true
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
