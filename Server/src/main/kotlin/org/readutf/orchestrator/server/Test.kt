package org.readutf.orchestrator.server

import org.readutf.orchestrator.server.settings.ApiSettings
import org.readutf.orchestrator.server.settings.DockerSettings
import org.readutf.orchestrator.server.settings.ServerSettings
import org.readutf.orchestrator.server.settings.Settings
import java.net.URI

fun main() {
    val settings =
        Settings(
            apiSettings =
                ApiSettings(
                    host = "localhost",
                    port = 9393,
                    virtualThreads = true,
                ),
            dockerSettings =
                DockerSettings(
                    uri = URI("http://localhost:2375"),
                    maxConnections = 100,
                    responseTimeout = java.time.Duration.ofSeconds(10),
                    connectionTimeout = java.time.Duration.ofSeconds(10),
                ),
            serverSettings =
                ServerSettings(
                    "localhost",
                    port = 2980,
                ),
        )
}
