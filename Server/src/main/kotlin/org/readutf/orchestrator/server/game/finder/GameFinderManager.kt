package org.readutf.orchestrator.server.game.finder

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.orchestrator.server.game.GameManager
import org.readutf.orchestrator.server.game.finder.impl.ExistingGameSearch
import org.readutf.orchestrator.server.game.finder.impl.GameCreatorSearch
import org.readutf.orchestrator.shared.game.GameRequest
import org.readutf.orchestrator.shared.game.GameRequestResult
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

class GameFinderManager(
    gameManager: GameManager,
) {
    private val gameFinderThread = Executors.newSingleThreadExecutor()
    private val logger = KotlinLogging.logger { }

    private val finders: List<GameFinder> =
        listOf(
            ExistingGameSearch(gameManager, 5),
            GameCreatorSearch(gameManager, 5),
        )

    /**
     * Recursively execute each finder on the finder thread,
     * allowing for partial steps to occur on different threads
     */
    fun findMatch(gameRequest: GameRequest): GameRequestResult {
        return CompletableFuture
            .supplyAsync({
                for (finder in finders) {
                    logger.info { "Using ${finder.gameFinderType} to find a match" }
                    val result = finder.findGame(gameRequest)
                    if (result.isSuccess()) return@supplyAsync result
                }
                return@supplyAsync GameRequestResult.failure(gameRequest.requestId, "No game server is available right now.")
            }, gameFinderThread)
            .join()
    }
}
