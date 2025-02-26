package org.readutf.orchestrator.server.features.games

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.map
import com.github.michaelbull.result.onSuccess
import org.readutf.orchestrator.common.game.Game
import org.readutf.orchestrator.common.game.GameFinderType
import org.readutf.orchestrator.common.game.GameServerSettings
import org.readutf.orchestrator.common.packets.S2CGameRequestPacket
import org.readutf.orchestrator.common.server.Server
import org.readutf.orchestrator.common.utils.SResult
import org.readutf.orchestrator.server.server.RegisteredServer
import org.readutf.orchestrator.server.server.ServerManager
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

class GameManager(
    private val serverManager: ServerManager,
    private val objectMapper: ObjectMapper,
) {

    private val requestExecutor = Executors.newFixedThreadPool(4)

    fun findGameServer(gameId: String): CompletableFuture<SResult<Game>> {
        val gameServers = serverManager.getServers()
            .sortedByDescending { it.getCapacity() }

        return CompletableFuture.supplyAsync({
            /**
             * Search servers for pre-existing 'pooled' games
             * If a server is found, return the server
             */
            gameServers.forEach { server ->
                val gameSettings = getGameSettings(server) ?: return@forEach
                if (gameSettings.supportedGames.none { it.equals(gameId, true) }) return@forEach

                if (gameSettings.finderTypes.contains(GameFinderType.POOLED)) {
                    findAvailableGame(server).join().onSuccess { game ->
                        return@supplyAsync Ok(game)
                    }
                }
            }

            /**
             * Search servers for servers that can create games on request
             * If a server is found, request a game
             */
            gameServers.forEach { server ->
                val gameSettings = getGameSettings(server) ?: return@forEach
                if (gameSettings.supportedGames.none { it.equals(gameId, true) }) return@forEach

                if (gameSettings.finderTypes.contains(GameFinderType.CREATE_ON_REQUEST)) {
                    requestGame(server, "", emptyList()).join().onSuccess { gameId ->
                        return@supplyAsync Ok(gameId)
                    }
                }
            }

            Err("No servers found")
        }, requestExecutor)
    }

    private fun findAvailableGame(server: RegisteredServer): CompletableFuture<SResult<Game>> {
        TODO()
    }

    private fun requestGame(
        server: RegisteredServer,
        gameType: String,
        players: List<UUID>,
    ): CompletableFuture<SResult<Game>> {
        val packet = S2CGameRequestPacket(gameType, players)

        return server.sendPacketFuture<UUID>(packet).thenApply { gameIdResult ->
            gameIdResult.map { gameId -> Game(gameId, server.serverId, true) }
        }
    }

    fun getGameSettings(server: Server): GameServerSettings? = objectMapper.convertValue(server.attributes["gameSettings"], GameServerSettings::class.java)
}
