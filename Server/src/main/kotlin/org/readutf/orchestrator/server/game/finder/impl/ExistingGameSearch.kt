package org.readutf.orchestrator.server.game.finder.impl

import org.readutf.orchestrator.server.game.GameManager
import org.readutf.orchestrator.server.game.finder.GameFinder
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.shared.game.GameFinderType
import org.readutf.orchestrator.shared.game.GameRequest
import org.readutf.orchestrator.shared.game.GameRequestResult
import panda.std.Result
import java.util.concurrent.CompletableFuture

class ExistingGameSearch(
    private val serverManager: ServerManager,
    private val gameManager: GameManager,
) : GameFinder(GameFinderType.PRE_EXISTING) {
    override fun findGame(gameRequest: GameRequest): CompletableFuture<Result<GameRequestResult, String>> {
//
//        val (server, games) =
//            availableGames.minByOrNull { it.value.size }
//                ?: return Result.error("No available games found")
//
//        return Result.ok(GameRequestResult(requestId = gameRequest.requestId, serverId = server.serverId, gameId = games.first().id))

        val availableGames = serverManager.findEmptyExistingGames(gameRequest.gameType)

        return CompletableFuture.supplyAsync {
            availableGames.sortedBy { it.first.activeGames.size }.forEach {
                val (server, game) = it

                val reserveResult = gameManager.reserveGame(game.id).join()
                if (reserveResult) return@supplyAsync Result.ok(GameRequestResult(gameRequest.requestId, server.serverId, game.id))
            }
            return@supplyAsync Result.error("No available games found")
        }
    }
}
