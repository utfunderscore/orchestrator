package org.readutf.orchestrator.server.game.finder.impl

import org.readutf.orchestrator.server.game.finder.GameFinder
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.shared.game.GameFinderType
import org.readutf.orchestrator.shared.game.GameRequest
import org.readutf.orchestrator.shared.game.GameRequestResult
import panda.std.Result

class ExistingGameSearch(
    private val serverManager: ServerManager,
) : GameFinder(GameFinderType.PRE_EXISTING) {
    override fun findGame(gameRequest: GameRequest): Result<GameRequestResult, String> {
        val availableGames = serverManager.findExistingGamesForSearch(gameRequest.gameType)

        val (server, games) =
            availableGames.minByOrNull { it.value.size }
                ?: return Result.error("No available games found")

        return Result.ok(GameRequestResult(requestId = gameRequest.requestId, serverId = server.serverId, gameId = games.first().id))
    }
}
