pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "orchestrator"
include("server")
include("common")
include("client")
include("mc-proxy")
include("api")
include("mc-edge-node")
include("controlpanel")
include("panel")
include("panel")
