package org.readutf.orchestrator.server.game.finder.impl

import org.readutf.orchestrator.server.game.finder.GameFinder
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.shared.game.Game
import org.readutf.orchestrator.shared.game.GameFinderType
import org.readutf.orchestrator.shared.game.GameRequest
import org.readutf.orchestrator.shared.game.GameRequestResult
import org.readutf.orchestrator.shared.server.Server
import panda.std.Result

class ExistingGamesSearch(
    private val serverManager: ServerManager,
) : GameFinder(GameFinderType.PRE_EXISTING) {
    override fun findGame(gameRequest: GameRequest): Result<GameRequestResult, String> {
        val availableGames = serverManager.findGamesByType(gameRequest.gameType)

        val emptyGames = mutableMapOf<Server, List<Game>>()

        availableGames.forEach { (server, games) ->
            val empty = games.filter { it.teams.all { team -> team.isEmpty() } }
            if (empty.isNotEmpty()) {
                emptyGames[server] = empty
            }
        }

        val (server, games) =
            emptyGames.minByOrNull { it.key.activeGames.size }
                ?: return Result.error("No available games found")

        return Result.ok(GameRequestResult(requestId = gameRequest.requestId, serverId = server.serverId, gameId = games.first().id))
    }

    private fun hasEmptyGame(games: List<Game>): Boolean = games.any { it.teams.all { team -> team.isEmpty() } }
}
