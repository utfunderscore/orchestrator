package org.readutf.orchestrator.server.game.finder.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.orchestrator.server.game.GameManager
import org.readutf.orchestrator.server.game.finder.GameFinder
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.shared.game.GameFinderType
import org.readutf.orchestrator.shared.game.GameRequest
import org.readutf.orchestrator.shared.game.GameRequestResult
import org.readutf.orchestrator.shared.utils.Result
import java.util.concurrent.Executor

class ExistingGameSearch(
    private val serverManager: ServerManager,
    private val executor: Executor,
    private val gameManager: GameManager,
) : GameFinder(GameFinderType.PRE_EXISTING) {
    private val logger = KotlinLogging.logger { }

    override fun findGame(gameRequest: GameRequest): Result<GameRequestResult> {
        val availableGames = serverManager.findEmptyExistingGames(gameRequest.gameType)

        availableGames.sortedBy { it.first.activeGames.size }.forEach {
            val (server, game) = it

            logger.info { "Requesting ${game.id}" }
            val reserveResult = gameManager.reserveGame(server.channel, game.id).join()

            if (reserveResult) {
                return Result.ok(GameRequestResult(gameRequest.requestId, server.serverId, game.id))
            }
        }
        return Result.error("No available games found")
    }
}
