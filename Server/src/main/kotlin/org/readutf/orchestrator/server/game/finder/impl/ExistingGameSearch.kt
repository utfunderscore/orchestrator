package org.readutf.orchestrator.server.game.finder.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.annotations.Blocking
import org.readutf.orchestrator.server.game.GameManager
import org.readutf.orchestrator.server.game.finder.GameFinder
import org.readutf.orchestrator.shared.game.GameFinderType
import org.readutf.orchestrator.shared.game.GameRequest
import org.readutf.orchestrator.shared.game.GameRequestResponse
import org.readutf.orchestrator.shared.game.GameReservation
import org.readutf.orchestrator.shared.utils.Result
import java.util.concurrent.TimeUnit

class ExistingGameSearch(
    private val gameManager: GameManager,
    private val maxReservations: Int,
) : GameFinder(GameFinderType.PRE_EXISTING) {
    private val logger = KotlinLogging.logger { }

    @Blocking
    override fun findGame(gameRequest: GameRequest): GameRequestResponse {
        val availableGames = gameManager.findEmptyExistingGames(gameRequest.gameType)

        availableGames
            .take(maxReservations)
            .forEach { (server, game) ->
                val reserveResult: Result<GameReservation> =
                    try {
                        gameManager
                            .reserveGame(server.channel, game.id)
                            .orTimeout(3000, TimeUnit.MILLISECONDS)
                            .join()
                    } catch (e: Exception) {
                        Result.error(e.message ?: "")
                    }

                if (reserveResult.isOk()) {
                    return GameRequestResponse.success(gameRequest.requestId, server.serverId, game.id)
                }
            }

        return GameRequestResponse.failure(gameRequest.requestId, "")
    }
}
