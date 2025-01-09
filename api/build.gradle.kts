plugins {
    kotlin("jvm")
}

group = "io.github.utfunderscore"
version = "2.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
}

tasks.test {
    useJUnitPlatform()
}
