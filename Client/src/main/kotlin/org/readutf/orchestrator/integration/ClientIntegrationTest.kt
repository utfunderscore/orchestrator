package org.readutf.orchestrator.integration

import org.readutf.orchestrator.client.ShepardClient
import org.readutf.orchestrator.shared.game.GameFinderType
import org.readutf.orchestrator.shared.game.GameState
import org.readutf.orchestrator.shared.server.ServerAddress
import java.util.*

class ClientIntegrationTest {
    private var client: ShepardClient =
        ShepardClient(
            serverAddress = ServerAddress("localhost", 25565),
            supportedGameTypes = listOf("test"),
            gameFinderTypes = mutableListOf(GameFinderType.PRE_EXISTING),
        )

    init {
        client.gameManager.registerGame(UUID.randomUUID(), "test", emptyList(), GameState.IDLE)
        client.gameManager.registerGame(UUID.randomUUID(), "test", emptyList(), GameState.IDLE)
        client.gameManager.registerGame(UUID.randomUUID(), "test", emptyList(), GameState.IDLE)
        client.gameManager.registerGame(UUID.randomUUID(), "test", emptyList(), GameState.IDLE)
        client.gameManager.registerGame(UUID.randomUUID(), "test", emptyList(), GameState.IDLE)
    }
}

fun main() {
    ClientIntegrationTest()
}
