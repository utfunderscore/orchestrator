package org.readutf.orchestrator.server.server.store.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.orchestrator.server.server.RegisteredServer
import org.readutf.orchestrator.server.server.store.ServerStore
import org.readutf.orchestrator.shared.server.ServerHeartbeat
import org.readutf.orchestrator.shared.utils.TypeWrapper
import java.util.*

class MemoryServerStore : ServerStore {
    private val logger = KotlinLogging.logger { }

    val servers = mutableMapOf<String, RegisteredServer>()
    private val channelServers = mutableMapOf<String, MutableList<String>>()

    override fun getServerById(serverId: String): RegisteredServer? = servers[serverId]

    override fun saveServer(registeredServer: RegisteredServer) {
        servers[registeredServer.serverId] = registeredServer
        channelServers
            .getOrPut(registeredServer.channel.channelId) { mutableListOf() }
            .add(registeredServer.serverId)
    }

    override fun removeServer(serverId: String): RegisteredServer? = servers.remove(serverId)

    override fun getServersByChannel(channelId: String): List<RegisteredServer> =
        channelServers[channelId]?.mapNotNull { servers[it] } ?: emptyList()

    override fun updateHeartbeat(
        serverId: String,
        serverHeartbeat: ServerHeartbeat,
    ) {
        servers[serverId]?.let {
            it.heartbeat = serverHeartbeat
        }
    }

    override fun getAllServers(): List<RegisteredServer> = servers.values.toList()

    override fun getServerByShortId(shortId: String): RegisteredServer? =
        servers.values.firstOrNull {
            it.serverId.toString().startsWith(shortId)
        }

    override fun setAttribute(
        serverId: String,
        attributeName: String,
        typedObject: Any,
    ) {
        val serverById =
            getServerById(serverId) ?: let {
                logger.warn { "Server attempting to update attribute before registering" }
                return
            }

        serverById.attributes[attributeName] = TypeWrapper(typedObject)
    }

    override fun removeAttribute(
        serverId: String,
        attributeName: String,
    ) {
        val serverById =
            getServerById(serverId) ?: let {
                logger.warn { "Server attempting to update attribute before registering" }
                return
            }

        serverById.attributes.remove(attributeName)
    }

    override fun getServersByType(gameType: String): List<RegisteredServer> =
        servers.values.filter { server -> server.serverType.equals(gameType, true) }

    override fun markServerForDeletion(serverId: String) {
        servers[serverId]?.let { server ->
            server.pendingDeletion = true
        }
    }

    override fun getTimedOutServers(): List<RegisteredServer> {
        val now = System.currentTimeMillis()
        return servers.values.filter { it.heartbeat.timestamp < now - 15000 }
    }
}
