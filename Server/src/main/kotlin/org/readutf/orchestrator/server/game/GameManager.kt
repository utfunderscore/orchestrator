package org.readutf.orchestrator.server.game

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.hermes.channel.HermesChannel
import org.readutf.orchestrator.server.game.finder.GameFinderManager
import org.readutf.orchestrator.server.game.store.GameStore
import org.readutf.orchestrator.server.server.RegisteredServer
import org.readutf.orchestrator.shared.game.Game
import org.readutf.orchestrator.shared.game.GameRequest
import org.readutf.orchestrator.shared.game.GameRequestResponse
import org.readutf.orchestrator.shared.game.GameReservation
import org.readutf.orchestrator.shared.packets.GameRequestPacket
import org.readutf.orchestrator.shared.packets.GameReservePacket
import org.readutf.orchestrator.shared.utils.Result
import java.util.UUID
import java.util.concurrent.CompletableFuture

class GameManager(
    private val gameStore: GameStore,
) {
    private val logger = KotlinLogging.logger { }
    private val gameFinderManager = GameFinderManager(this)

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

    fun findMatch(gameRequest: GameRequest) = gameFinderManager.findMatch(gameRequest)

    fun reserveGame(
        channel: HermesChannel,
        gameId: UUID,
    ): CompletableFuture<Result<GameReservation>> {
        val packet = GameReservePacket(gameId, UUID.randomUUID())
        val future = channel.sendPacketFuture<Result<GameReservation>>(packet)
        return future.thenApply {
            if (it.isOk()) gameStore.setReservation(gameId, it.get())
            return@thenApply it
        }
    }

    fun updateGames(
        serverId: UUID,
        games: List<Game>,
    ) {
        logger.debug { "Updating games for server $serverId" }

        gameStore.setGames(serverId, games)
    }

    fun getGamesByServer(serverId: UUID) = gameStore.getGamesByServerId(serverId)

    fun findGameRequestServers(sortByActiveGames: Boolean): List<RegisteredServer> = gameStore.findGameRequestServers(sortByActiveGames)

    fun requestGame(
        findGameRequestServer: RegisteredServer,
        gameRequest: GameRequest,
    ): CompletableFuture<GameRequestResponse> =
        findGameRequestServer.channel.sendPacketFuture<GameRequestResponse>(GameRequestPacket(gameRequest))
}
