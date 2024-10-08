@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.readutf.orchestrator.server.server

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.orchestrator.server.notification.NotificationManager
import org.readutf.orchestrator.server.server.store.ServerStore
import org.readutf.orchestrator.shared.notification.impl.ServerRegisterNotification
import org.readutf.orchestrator.shared.notification.impl.ServerUnregisterNotification
import org.readutf.orchestrator.shared.server.Server
import org.readutf.orchestrator.shared.server.ServerHeartbeat
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.*

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
            SECONDS,
        )
    }

    fun registerServer(server: RegisteredServer) {
        logger.info { "Registering server ${server.serverId}" }

        NotificationManager.notifyAll(ServerRegisterNotification(server))

        serverStore.saveServer(server)
    }

    fun getAllServers(): List<RegisteredServer> = serverStore.getAllServers()

    fun unregisterServer(serverId: UUID) {
        logger.info { "Unregistering server $serverId" }

        NotificationManager.notifyAll(ServerUnregisterNotification(serverId))

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

    fun setAttribute(
        serverId: UUID,
        attributeName: String,
        typedObject: Any,
    ) {
        serverStore.setAttribute(serverId, attributeName, typedObject)
    }

    fun removeAttribute(
        serverId: UUID,
        attributeName: String,
    ) {
        serverStore.removeAttribute(serverId, attributeName)
    }

    fun getServerByShortId(shortId: String): Server? = serverStore.getServerByShortId(shortId)

    fun getServerById(serverId: UUID): Server? = serverStore.getServerById(serverId)

    fun getServersByType(serverType: String): List<Server> = serverStore.getServersByType(serverType)
}
