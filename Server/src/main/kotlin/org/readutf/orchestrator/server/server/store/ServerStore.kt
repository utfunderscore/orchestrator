package org.readutf.orchestrator.server.server.store

import org.readutf.orchestrator.server.server.RegisteredServer
import org.readutf.orchestrator.shared.server.Server
import org.readutf.orchestrator.shared.server.ServerHeartbeat
import java.util.UUID

interface ServerStore {
    fun saveServer(registeredServer: RegisteredServer)

    fun removeServer(serverId: UUID)

    fun getServersByChannel(channelId: String): List<RegisteredServer>

    fun updateHeartbeat(
        serverId: UUID,
        serverHeartbeat: ServerHeartbeat,
    )

    fun getTimedOutServers(): List<RegisteredServer>

    fun getServerById(serverId: UUID): RegisteredServer?

    fun getAllServers(): List<RegisteredServer>

    fun getServerByShortId(shortId: String): Server?

    fun setAttribute(
        serverId: UUID,
        attributeName: String,
        typedObject: Any,
    )

    fun removeAttribute(
        serverId: UUID,
        attributeName: String,
    )

    fun getServersByType(gameType: String): List<Server>
}
