@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.readutf.orchestrator.server.server

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.orchestrator.server.server.store.ServerStore
import org.readutf.orchestrator.shared.game.Game
import org.readutf.orchestrator.shared.server.Server
import org.readutf.orchestrator.shared.server.ServerHeartbeat
import java.util.*
import java.util.concurrent.Executors

class ServerManager(
    private val serverStore: ServerStore,
) {
    private val logger = KotlinLogging.logger { }
    private val scheduledExecutor = Executors.newSingleThreadScheduledExecutor()

    init {
        scheduledExecutor.scheduleAtFixedRate(
            { invalidateExpiredServers() },
            0,
            5,
            java.util.concurrent.TimeUnit.SECONDS,
        )
    }

    fun registerServer(server: RegisteredServer) {
        logger.info { "Registering server ${server.server.serverId}" }

        serverStore.saveServer(server)
    }

    fun unregisterServer(serverId: UUID) {
        logger.info { "Unregistering server $serverId" }

        serverStore.removeServer(serverId)
    }

    fun unregisterChannel(channelId: String) {
        logger.info { "Unregistering socket $channelId" }

        serverStore.getServersByChannel(channelId).forEach { unregisterServer(it.server.serverId) }
    }

    private fun invalidateExpiredServers() {
        serverStore.getTimedOutServers().forEach {
            logger.info { "Server ${it.server.serverId} has timed out" }
            unregisterServer(it.server.serverId)
        }
    }

    fun findGamesByType(gameType: String): Map<Server, List<Game>> {
        logger.info { "Finding games by type $gameType" }

        return serverStore.findGamesByType(gameType)
    }

    /**
     * Used in ExistingGameSearch to find server that are
     * empty, valid game type, and support that game finder
     */
    fun findExistingGamesForSearch(gameType: String): Map<Server, List<Game>> = serverStore.findExistingGamesForSearch(gameType)

    fun handleHeartbeat(serverHeartbeat: ServerHeartbeat) {
        logger.debug { "Received heartbeat from ${serverHeartbeat.serverId}" }

        serverStore.updateHeartbeat(serverHeartbeat.serverId, serverHeartbeat)
    }

    fun updateGames(
        serverId: UUID,
        games: List<Game>,
    ) {
        logger.info { "Updating games for server $serverId" }

        serverStore.setGames(serverId, games)
    }
}
