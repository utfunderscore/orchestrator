package org.readutf.orchestrator.server

import org.readutf.orchestrator.server.docker.DockerManager
import org.readutf.orchestrator.server.settings.DockerSettings
import java.util.*

fun main() {
    val dockerManager =
        DockerManager(
            DockerSettings(
                uri = "tcp://localhost:2375",
                maxConnections = 100,
                responseTimeout = 30,
                connectionTimeout = 30,
            ),
        )
}
