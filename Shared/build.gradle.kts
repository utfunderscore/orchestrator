plugins {
    kotlin("jvm")
    id("java-library")
}

group = "org.readutf.orchestrator"
version = "1.0-SNAPSHOT"

dependencies {
    // add fastjson2
    implementation("com.alibaba:fastjson:2.0.51")

    api("org.panda-lang:expressible:1.3.6") // Core library
    api("org.panda-lang:expressible-kt:1.3.6") // Kotlin extensions

    implementation("io.netty:netty-all:4.1.111.Final")

    //Add fastjson2
    compileOnly("com.alibaba:fastjson:2.0.51")

    implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")

}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
