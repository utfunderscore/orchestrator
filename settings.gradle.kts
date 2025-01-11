pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

toolchainManagement {
}

rootProject.name = "orchestrator"
include("server")
include("common")
include("client")
include("mc-proxy")
include("api")
include("mc-edge-node")
