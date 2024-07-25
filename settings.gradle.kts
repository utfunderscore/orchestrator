plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "orchestrator"
include("Shared")
include("Server")
include("Client")
include("ApiWrapper")
