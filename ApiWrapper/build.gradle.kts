plugins {
    kotlin("jvm")
}

group = "org.readutf.orchestrator"
version = "1.0-SNAPSHOT"

dependencies {
    testImplementation(kotlin("test"))

    implementation(project(":Shared"))

    implementation("com.alibaba.fastjson2:fastjson2:2.0.52")
    implementation("org.java-websocket:Java-WebSocket:1.5.7")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
