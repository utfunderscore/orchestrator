package org.readutf.orchestrator.server.server.store

import org.readutf.orchestrator.server.server.RegisteredServer
import org.readutf.orchestrator.shared.server.ServerHeartbeat
import java.util.UUID

interface ServerStore {
    fun saveServer(registeredServer: RegisteredServer)

    fun removeServer(serverId: UUID)

    fun getServersByChannel(channelId: String): List<RegisteredServer>

    fun removeServerByChannel(channelId: String)

    fun updateHeartbeat(
        serverId: UUID,
        serverHeartbeat: ServerHeartbeat,
    )

    fun getServerById(serverId: UUID): RegisteredServer?
}
