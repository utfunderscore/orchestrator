package org.readutf.orchestrator.server.features.games

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.github.michaelbull.result.onSuccess
import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.orchestrator.common.game.Game
import org.readutf.orchestrator.common.game.GameFinderType
import org.readutf.orchestrator.common.game.GameServerSettings
import org.readutf.orchestrator.common.packets.S2CGameRequestPacket
import org.readutf.orchestrator.common.server.Server
import org.readutf.orchestrator.server.server.RegisteredServer
import org.readutf.orchestrator.server.server.ServerManager
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

class GameManager(
    private val serverManager: ServerManager,
    private val objectMapper: ObjectMapper,
) {

    private val logger = KotlinLogging.logger { }

    private val requestExecutor = Executors.newFixedThreadPool(4)

    fun findGameServer(gameType: String, players: List<UUID>): CompletableFuture<Result<GameServer, Throwable>> {
        val gameServers = serverManager.getServers()
            .sortedByDescending { it.getCapacity() }

        return CompletableFuture.supplyAsync({
            /**
             * Search servers for pre-existing 'pooled' games
             * If a server is found, return the server
             */
            gameServers.forEach { server ->
                val gameSettings = getGameSettings(server) ?: return@forEach
                if (gameSettings.supportedGames.none { it.equals(gameType, true) }) return@forEach

                if (gameSettings.finderTypes.contains(GameFinderType.POOLED)) {
                    findAvailableGame(server).join().onSuccess { game ->
                        return@supplyAsync Ok(GameServer(server, game))
                    }
                }
            }

            /**
             * Search servers for servers that can create games on request
             * If a server is found, request a game
             */
            gameServers.forEach { server ->
                val gameSettings = getGameSettings(server) ?: let {
                    logger.info { "No game settings found for ${server.displayName}" }
                    return@forEach
                }
                if (gameSettings.supportedGames.none { it.equals(gameType, true) }) {
                    logger.info { "Game type $gameType not supported by ${server.displayName}" }
                    return@forEach
                }

                if (gameSettings.finderTypes.contains(GameFinderType.CREATE_ON_REQUEST)) {
                    requestGame(server, gameType, players).join().onSuccess { gameId ->
                        return@supplyAsync Ok(GameServer(server, gameId))
                    }
                } else {
                    logger.info { "Server ${server.displayName} does not support CREATE_ON_REQUEST" }
                }
            }

            Err(Exception("No servers found"))
        }, requestExecutor)
    }

    private fun findAvailableGame(server: RegisteredServer): CompletableFuture<Result<Game, Throwable>> {
        TODO()
    }

    private fun requestGame(
        server: RegisteredServer,
        gameType: String,
        players: List<UUID>,
    ): CompletableFuture<Result<Game, Throwable>> {
        logger.info { "Sending game request to ${server.displayName}" }

        val packet = S2CGameRequestPacket(gameType, players)

        return server.sendPacketFuture<UUID>(packet).thenApply { gameIdResult ->
            gameIdResult.map { gameId -> Game(gameId, true) }
        }
    }

    fun getGameSettings(server: Server): GameServerSettings? = objectMapper.convertValue(server.attributes["gameSettings"], GameServerSettings::class.java)
}
