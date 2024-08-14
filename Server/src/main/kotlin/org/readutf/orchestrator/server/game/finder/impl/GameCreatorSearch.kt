package org.readutf.orchestrator.server.game.finder.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.orchestrator.server.game.GameManager
import org.readutf.orchestrator.server.game.finder.GameFinder
import org.readutf.orchestrator.shared.game.GameFinderType
import org.readutf.orchestrator.shared.game.GameRequest
import org.readutf.orchestrator.shared.game.GameRequestResult

class GameCreatorSearch(
    private val gameManager: GameManager,
    private val maxRequests: Int,
) : GameFinder(GameFinderType.ON_REQUEST) {
    private val logger = KotlinLogging.logger { }

    override fun findGame(gameRequest: GameRequest): GameRequestResult {
        for (registeredServer in gameManager.findGameRequestServers(true).take(maxRequests)) {
            logger.info { "Sending request to ${registeredServer.serverId}" }

            val packetResult = gameManager.requestGame(registeredServer, gameRequest).join()
            if (packetResult.isSuccess()) return packetResult
        }
        return GameRequestResult.failure(gameRequest.requestId, "")
    }
}
