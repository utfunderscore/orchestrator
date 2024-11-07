package org.readutf.orchestrator.client.demo

import org.readutf.orchestrator.client.OrchestratorClient
import org.readutf.orchestrator.shared.server.ServerAddress
import java.util.UUID

fun main() {
    DemoClient()
}

class DemoClient {
    init {

        val serverId = UUID.randomUUID().toString()

        val client =
            OrchestratorClient(
                serverId = serverId,
                orchestratorHost = "localhost",
                orchestratorPort = 2980,
                serverAddress = ServerAddress("localhost", 25565),
                serverType = "lobby",
                shutdownRequestHandler = {
                },
                onConnect = {
                },
            )

        client.connect()
    }
}
