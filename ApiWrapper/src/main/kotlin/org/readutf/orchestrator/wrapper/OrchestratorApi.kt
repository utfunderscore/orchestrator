package org.readutf.orchestrator.wrapper

import org.readutf.orchestrator.shared.game.GameRequest

object OrchestratorApi {
    private val requestClient by lazy { GameRequestClient("ws://localhost:8080") }

    fun requestGame(
        gameRequest: GameRequest,
        timeout: Long = 5000,
    ) {
        requestClient.requestGame(gameRequest.gameType, timeout)
    }
}
