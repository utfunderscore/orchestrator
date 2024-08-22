package org.readutf.orchestrator.integration

import org.readutf.orchestrator.client.ShepardClient
import org.readutf.orchestrator.shared.game.GameFinderType
import org.readutf.orchestrator.shared.server.ServerAddress
import java.util.UUID

class ClientIntegrationTest {
    private var client: ShepardClient =
        ShepardClient(
            "localhost",
            2980,
            serverAddress = ServerAddress("localhost", 25565),
        ).registerGameTypes("test")
            .registerFinderTypes(GameFinderType.PRE_EXISTING, GameFinderType.ON_REQUEST)

    init {
        client.start()

        client.setGameRequestHandler {
            UUID.randomUUID()
        }
    }
}

fun main() {
    ClientIntegrationTest()
}
