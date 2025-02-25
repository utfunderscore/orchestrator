import com.vanniktech.maven.publish.SonatypeHost

plugins {
    `java-library`
    kotlin("jvm") version "2.1.0"
    id("com.vanniktech.maven.publish") version "0.30.0"
    `maven-publish`
    signing
}

repositories {
    mavenCentral()
}

val hermesVersion = rootProject.extra["hermesVersion"] as String

dependencies {
    testImplementation(kotlin("test"))

    api("com.michael-bull.kotlin-result:kotlin-result:2.0.1")

    compileOnly("io.javalin:javalin:6.4.0")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.0")

    compileOnly("io.github.utfunderscore.hermes:hermes.core:$hermesVersion")
    compileOnly("io.github.utfunderscore.hermes:hermes.netty:$hermesVersion")
    compileOnly("io.github.utfunderscore.hermes:hermes.kryo:$hermesVersion")
    compileOnly("com.esotericsoftware:kryo:5.6.2")

    compileOnly("com.fasterxml.jackson.core:jackson-databind:2.18.2")
    compileOnly("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.2")
}

publishing {
    publications {
        create<MavenPublication>("maven2") {
            groupId = "org.readutf.orchestrator"
            version = rootProject.version.toString()
            artifactId = project.name

            from(components["java"])
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

tasks.named("publishMavenJavaPublicationToMavenCentralRepository") {
    dependsOn("signMavenPublication")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}

signing {
    sign(publishing.publications)
}
