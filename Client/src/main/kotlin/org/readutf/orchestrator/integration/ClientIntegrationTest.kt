package org.readutf.orchestrator.integration

import org.readutf.orchestrator.client.ShepardClient
import org.readutf.orchestrator.client.game.ActiveGameSupplier
import org.readutf.orchestrator.client.game.GameRequestHandler
import org.readutf.orchestrator.shared.game.Game
import org.readutf.orchestrator.shared.game.GameFinderType
import org.readutf.orchestrator.shared.game.GameRequest
import org.readutf.orchestrator.shared.game.GameRequestResult
import org.readutf.orchestrator.shared.game.GameState
import org.readutf.orchestrator.shared.server.ServerAddress
import org.readutf.orchestrator.shared.utils.Result
import java.util.UUID

class ClientIntegrationTest {
    private val gameSupplier =
        object : ActiveGameSupplier {
            override fun getActiveGames(): List<Game> = games
        }

    private val gameRequestHandler =
        object : GameRequestHandler {
            override fun handleRequest(request: GameRequest): Result<GameRequestResult> = error("Unsupported")
        }

    private var client: ShepardClient =
        ShepardClient(
            serverAddress = ServerAddress("localhost", 25565),
            supportedGameTypes = listOf("test"),
            gameFinderTypes = mutableListOf(GameFinderType.PRE_EXISTING),
            gameRequestHandler = gameRequestHandler,
        )


    init {
        client.gameManager.registerGame(UUID.randomUUID(), "test", emptyList(), GameState.)
    }
}

fun main() {
    ClientIntegrationTest()
}
