@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.readutf.orchestrator.server.server

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.orchestrator.server.server.store.ServerStore
import org.readutf.orchestrator.shared.server.ServerHeartbeat
import java.util.*

class ServerManager(
    private val serverStore: ServerStore,
) {
    private val logger = KotlinLogging.logger { }

    fun registerServer(server: RegisteredServer) {
        logger.info { "Registering server ${server.serverId}" }

        serverStore.saveServer(server)
    }

    fun unregisterServer(serverId: UUID) {
        logger.info { "Unregistering server $serverId" }

        serverStore.removeServer(serverId)
    }

    fun unregisterChannel(channelId: String) {
        logger.info { "Unregistering socket $channelId" }

        serverStore.removeServerByChannel(channelId)
    }

    fun handleHeartbeat(serverHeartbeat: ServerHeartbeat) {
        logger.debug { "Received heartbeat from ${serverHeartbeat.serverId}" }

        serverStore.updateHeartbeat(serverHeartbeat.serverId, serverHeartbeat)
    }
}
