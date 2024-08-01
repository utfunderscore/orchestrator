package org.readutf.orchestrator.server.server.store.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.orchestrator.server.server.RegisteredServer
import org.readutf.orchestrator.server.server.store.ServerStore
import org.readutf.orchestrator.shared.server.Server
import org.readutf.orchestrator.shared.server.ServerHeartbeat
import java.util.*

class MemoryServerStore : ServerStore {
    private val logger = KotlinLogging.logger { }

    val servers = mutableMapOf<UUID, RegisteredServer>()
    private val channelServers = mutableMapOf<String, MutableList<UUID>>()

    override fun getServerById(serverId: UUID): RegisteredServer? = servers[serverId]

    override fun saveServer(registeredServer: RegisteredServer) {
        servers[registeredServer.serverId] = registeredServer
        channelServers
            .getOrPut(registeredServer.channel.channelId) { mutableListOf() }
            .add(registeredServer.serverId)
    }

    override fun removeServer(serverId: UUID) {
        servers.remove(serverId)
    }

    override fun getServersByChannel(channelId: String): List<RegisteredServer> =
        channelServers[channelId]?.mapNotNull { servers[it] } ?: emptyList()

    override fun updateHeartbeat(
        serverId: UUID,
        serverHeartbeat: ServerHeartbeat,
    ) {
        servers[serverId]?.let {
            it.heartbeat = serverHeartbeat
        }
    }

    override fun getAllServers(): List<RegisteredServer> = servers.values.toList()

    override fun getServerByShortId(shortId: String): Server? = servers.values.first { it.serverId.toString().startsWith(shortId) }

    override fun getTimedOutServers(): List<RegisteredServer> {
        val now = System.currentTimeMillis()
        return servers.values.filter { it.heartbeat.timestamp < now - 15000 }
    }
}