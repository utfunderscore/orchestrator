package org.readutf.orchestrator.server.server.store.impl

import org.readutf.orchestrator.server.server.RegisteredServer
import org.readutf.orchestrator.server.server.store.ServerStore
import org.readutf.orchestrator.shared.server.ServerHeartbeat
import java.util.UUID

class MemoryServerStore : ServerStore {
    private val servers = mutableMapOf<UUID, RegisteredServer>()
    private val channelServers = mutableMapOf<String, MutableList<UUID>>()

    override fun saveServer(registeredServer: RegisteredServer) {
        servers[registeredServer.serverId] = registeredServer
        channelServers
            .getOrDefault(registeredServer.channel.channelId, mutableListOf())
            .add(registeredServer.serverId)
    }

    override fun removeServer(serverId: UUID) {
        servers.remove(serverId)
    }

    override fun getServersByChannel(channelId: String): List<RegisteredServer> =
        channelServers[channelId]?.mapNotNull { servers[it] } ?: emptyList()

    override fun removeServerByChannel(channelId: String) {
        channelServers[channelId]?.forEach { serverId ->
            removeServer(serverId)
        }
    }

    override fun updateHeartbeat(
        serverId: UUID,
        serverHeartbeat: ServerHeartbeat,
    ) {
        servers[serverId]?.lastHeartbeat = serverHeartbeat.timestamp
    }
}
