package org.readutf.orchestrator.integration

import org.readutf.orchestrator.client.ShepardClient
import org.readutf.orchestrator.shared.game.GameFinderType
import org.readutf.orchestrator.shared.game.GameState
import org.readutf.orchestrator.shared.server.ServerAddress
import java.util.UUID

class ClientIntegrationTest {
    private var client: ShepardClient =
        ShepardClient(
            serverAddress = ServerAddress("localhost", 25565),
        ).registerGameTypes("test")
            .registerFinderTypes(GameFinderType.PRE_EXISTING, GameFinderType.ON_REQUEST)

    init {
        client.start()

        client.registerGame(UUID.randomUUID(), "test", emptyList(), GameState.IDLE)
        client.registerGame(UUID.randomUUID(), "test", emptyList(), GameState.IDLE)
        client.registerGame(UUID.randomUUID(), "test", emptyList(), GameState.IDLE)
        client.registerGame(UUID.randomUUID(), "test", emptyList(), GameState.IDLE)
        client.registerGame(UUID.randomUUID(), "test", emptyList(), GameState.IDLE)
    }
}

fun main() {
    ClientIntegrationTest()
}
