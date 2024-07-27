@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.readutf.orchestrator.server.server

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.orchestrator.server.server.store.DataStore
import org.readutf.orchestrator.shared.game.Game
import org.readutf.orchestrator.shared.server.ServerHeartbeat
import java.util.*
import java.util.concurrent.Executors

class ServerManager(
    private val dataStore: DataStore,
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
        logger.info { "Registering server ${server.serverId}" }

        dataStore.saveServer(server)
    }

    fun getAllServers(): List<RegisteredServer> = dataStore.getAllServers()

    fun unregisterServer(serverId: UUID) {
        logger.info { "Unregistering server $serverId" }

        dataStore.removeServer(serverId)
    }

    fun unregisterChannel(channelId: String) {
        logger.info { "Unregistering socket $channelId" }

        dataStore.getServersByChannel(channelId).forEach { unregisterServer(it.serverId) }
    }

    private fun invalidateExpiredServers() {
        dataStore.getTimedOutServers().forEach {
            logger.info { "Server ${it.serverId} has timed out" }
            unregisterServer(it.serverId)
        }
    }

    /**
     * Used in ExistingGameSearch to find server that are
     * empty, valid game type, and support that game finder
     */
    fun findEmptyExistingGames(gameType: String): List<Pair<RegisteredServer, Game>> = dataStore.findEmptyExistingGames(gameType)

    fun handleHeartbeat(serverHeartbeat: ServerHeartbeat) {
        logger.debug { "Received heartbeat from ${serverHeartbeat.serverId}" }

        dataStore.updateHeartbeat(serverHeartbeat.serverId, serverHeartbeat)
    }

    fun updateGames(
        serverId: UUID,
        games: List<Game>,
    ) {
        logger.debug { "Updating games for server $serverId" }

        dataStore.setGames(serverId, games)
    }
}
