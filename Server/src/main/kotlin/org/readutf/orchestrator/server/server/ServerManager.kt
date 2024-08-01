@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.readutf.orchestrator.server.server

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.orchestrator.server.server.store.ServerStore
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
        logger.info { "Registering server ${server.serverId}" }

        serverStore.saveServer(server)
    }

    fun getAllServers(): List<RegisteredServer> = serverStore.getAllServers()

    fun unregisterServer(serverId: UUID) {
        logger.info { "Unregistering server $serverId" }

        serverStore.removeServer(serverId)
    }

    fun unregisterChannel(channelId: String) {
        logger.info { "Unregistering socket $channelId" }

        serverStore.getServersByChannel(channelId).forEach { unregisterServer(it.serverId) }
    }

    private fun invalidateExpiredServers() {
        serverStore.getTimedOutServers().forEach {
            logger.info { "Server ${it.serverId} has timed out" }
            unregisterServer(it.serverId)
        }
    }

    fun handleHeartbeat(serverHeartbeat: ServerHeartbeat) {
        logger.debug { "Received heartbeat from ${serverHeartbeat.serverId}" }

        serverStore.updateHeartbeat(serverHeartbeat.serverId, serverHeartbeat)
    }

    fun getServerByShortId(shortId: String): Server? = serverStore.getServerByShortId(shortId)
}
