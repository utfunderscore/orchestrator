package org.readutf.orchestrator.server.game.finder.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.annotations.Blocking
import org.readutf.orchestrator.server.game.GameManager
import org.readutf.orchestrator.server.game.finder.GameFinder
import org.readutf.orchestrator.shared.game.GameFinderType
import org.readutf.orchestrator.shared.game.GameRequest
import org.readutf.orchestrator.shared.game.GameRequestResult

class ExistingGameSearch(
    private val gameManager: GameManager,
    private val maxReservations: Int,
) : GameFinder(GameFinderType.PRE_EXISTING) {
    private val logger = KotlinLogging.logger { }

    @Blocking
    override fun findGame(gameRequest: GameRequest): GameRequestResult {
        val availableGames = gameManager.findEmptyExistingGames(gameRequest.gameType)

        availableGames
            .take(maxReservations)
            .forEach { (server, game) ->
                val reserveResult = gameManager.reserveGame(server.channel, game.id).join()

                if (reserveResult) {
                    return GameRequestResult.success(gameRequest.requestId, server.serverId, game.id)
                }
            }

        return GameRequestResult.failure(gameRequest.requestId, "")
    }
}
