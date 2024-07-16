package org.readutf.orchestrator.server.server

import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.*

class ServerManager {
    private val logger = KotlinLogging.logger { }

    private val servers = mutableMapOf<UUID, RegisteredServer>()
    private val channelToServer = mutableMapOf<String, UUID>()

    fun registerServer(server: RegisteredServer) {
        logger.info { "Registering server ${server.serverId}" }

        servers[server.serverId] = server
    }

    fun unRegisterServer(serverId: UUID) {
        logger.info { "Unregistering server $serverId" }

        servers.remove(serverId)
    }
}
