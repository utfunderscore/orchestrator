package org.readutf.orchestrator.server.game

import io.github.oshai.kotlinlogging.KotlinLogging
import io.javalin.Javalin
import org.readutf.hermes.PacketManager
import org.readutf.hermes.channel.HermesChannel
import org.readutf.orchestrator.server.game.endpoints.GameRequestSocket
import org.readutf.orchestrator.server.game.finder.GameFinder
import org.readutf.orchestrator.server.game.finder.impl.ExistingGameSearch
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.shared.game.GameRequest
import org.readutf.orchestrator.shared.game.GameRequestResult
import org.readutf.orchestrator.shared.packets.GameReservePacket
import panda.std.Result
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

class GameManager(
    javalin: Javalin,
    serverManager: ServerManager,
    val packetManager: PacketManager<*>,
) {
    init {
        javalin.ws("/game/request", GameRequestSocket(this))
    }

    private val logger = KotlinLogging.logger { }
    private val gameFinderThread = Executors.newSingleThreadExecutor()

    private val finders: List<GameFinder> =
        listOf(
            ExistingGameSearch(serverManager, gameFinderThread, this),
        )

    fun findMatch(gameRequest: GameRequest): CompletableFuture<Result<GameRequestResult, String>> =
        CompletableFuture.supplyAsync({
            applyMatchFinders(gameRequest)
        }, gameFinderThread)

    fun reserveGame(
        channel: HermesChannel,
        gameId: UUID,
    ): CompletableFuture<Boolean> {
        val packet = GameReservePacket(gameId)
        println("ID: ${packet.packetId}")
        return channel.sendPacketFuture<Boolean>(packet)
    }

    private fun applyMatchFinders(gameRequest: GameRequest): Result<GameRequestResult, String> {
        finders.forEach { finder ->
            logger.info { "Using ${finder.gameFinderType.name}" }

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
