package org.readutf.orchestrator.wrapper

object OrchestratorApi {
    private val requestClient by lazy { GameRequestClient("ws://localhost:9393/game/request") }

    fun requestGame(
        gameType: String,
        timeout: Long = 5000,
    ) = requestClient.requestGame(gameType, timeout)
}
