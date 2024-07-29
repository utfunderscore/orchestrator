package org.readutf.orchestrator.server.game

import io.github.oshai.kotlinlogging.KotlinLogging
import io.javalin.Javalin
import org.readutf.hermes.channel.HermesChannel
import org.readutf.orchestrator.server.game.endpoints.GameRequestSocket
import org.readutf.orchestrator.server.game.finder.GameFinder
import org.readutf.orchestrator.server.game.finder.impl.ExistingGameSearch
import org.readutf.orchestrator.server.game.store.GameStore
import org.readutf.orchestrator.server.server.RegisteredServer
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.shared.game.Game
import org.readutf.orchestrator.shared.game.GameRequest
import org.readutf.orchestrator.shared.game.GameRequestResult
import org.readutf.orchestrator.shared.packets.GameReservePacket
import org.readutf.orchestrator.shared.utils.Result
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

class GameManager(
    javalin: Javalin,
    serverManager: ServerManager,
    val gameStore: GameStore,
) {
    init {
        javalin.ws("/game/request", GameRequestSocket(this, serverManager))
    }

    private val logger = KotlinLogging.logger { }
    private val gameFinderThread = Executors.newSingleThreadExecutor()

    private val finders: List<GameFinder> =
        listOf(
            ExistingGameSearch(serverManager, gameFinderThread, this),
        )

    fun registerGame(game: Game) {
        logger.info { "Registered game ${game.id}" }

        gameStore.save(game)
    }

    fun unRegisterGame(game: Game) {
        logger.info { "Unregistered game ${game.id}" }

        gameStore.remove(game)
    }

    /**
     * Used in ExistingGameSearch to find server that are
     * empty, valid game type, and support that game finder
     */
    fun findEmptyExistingGames(gameType: String): List<Pair<RegisteredServer, Game>> = gameStore.findEmptyExistingGames(gameType)

    fun findMatch(gameRequest: GameRequest): CompletableFuture<Result<GameRequestResult>> =
        CompletableFuture.supplyAsync({
            applyMatchFinders(gameRequest)
        }, gameFinderThread)

    fun reserveGame(
        channel: HermesChannel,
        gameId: UUID,
    ): CompletableFuture<Boolean> {
        val packet = GameReservePacket(gameId)
        return channel.sendPacketFuture<Boolean>(packet)
    }

    private fun applyMatchFinders(gameRequest: GameRequest): Result<GameRequestResult> {
        finders.forEach { finder ->
            logger.info { "Using ${finder.gameFinderType.name}" }

            val findGameResult = finder.findGame(gameRequest)
            if (findGameResult.isError()) {
                logger.info { "Game could not be found with ${finder.gameFinderType.name}" }
            } else {
                return Result.ok(findGameResult.get())
            }
        }
        return Result.error("Could not find a game type")
    }

    fun updateGames(
        serverId: UUID,
        games: List<Game>,
    ) {
        logger.debug { "Updating games for server $serverId" }

        gameStore.setGames(serverId, games)
    }

    fun getGamesByServer(serverId: UUID) = gameStore.getGamesByServerId(serverId)

    fun getGameById(gameId: UUID) = gameStore.getGameById(gameId)
}
