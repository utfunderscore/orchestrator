package org.readutf.orchestrator.server.game

import io.github.oshai.kotlinlogging.KotlinLogging
import io.javalin.Javalin
import org.readutf.orchestrator.server.game.endpoints.GameRequestSocket
import org.readutf.orchestrator.server.game.finder.GameFinder
import org.readutf.orchestrator.server.game.finder.impl.ExistingGameSearch
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.shared.game.GameRequest
import org.readutf.orchestrator.shared.game.GameRequestResult
import panda.std.Result
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

class GameManager(
    javalin: Javalin,
    serverManager: ServerManager,
) {
    init {
        javalin.ws("/game/request", GameRequestSocket(this))
    }

    private val logger = KotlinLogging.logger { }
    private val gameFinderThread = Executors.newSingleThreadExecutor()

    private val finders: List<GameFinder> =
        listOf(
            ExistingGameSearch(serverManager),
        )

    fun findMatch(gameRequest: GameRequest): CompletableFuture<Result<GameRequestResult, String>> =
        CompletableFuture.supplyAsync({
            applyMatchFinders(gameRequest)
        }, gameFinderThread)

    private fun applyMatchFinders(gameRequest: GameRequest): Result<GameRequestResult, String> {
        finders.forEach { finder ->

            val findGameResult = finder.findGame(gameRequest)
            if (findGameResult.isErr) {
                logger.info { "Game could not be found with ${finder.gameFinderType.name}" }
            } else {
                return Result.ok(findGameResult.get())
            }
        }
        return Result.error("Could not find a game type")
    }
}
