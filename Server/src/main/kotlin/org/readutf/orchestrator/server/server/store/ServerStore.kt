package org.readutf.orchestrator.server.server.store

import org.readutf.orchestrator.server.server.RegisteredServer
import org.readutf.orchestrator.shared.server.Server
import org.readutf.orchestrator.shared.server.ServerHeartbeat

interface ServerStore {
    fun saveServer(registeredServer: RegisteredServer)

    fun removeServer(serverId: String): RegisteredServer?

    fun getServersByChannel(channelId: String): List<RegisteredServer>

    fun updateHeartbeat(
        serverId: String,
        serverHeartbeat: ServerHeartbeat,
    )

    fun getTimedOutServers(): List<RegisteredServer>

    fun getServerById(serverId: String): RegisteredServer?

    fun getAllServers(): List<RegisteredServer>

    fun getServerByShortId(shortId: String): Server?

    fun setAttribute(
        serverId: String,
        attributeName: String,
        typedObject: Any,
    )

    fun removeAttribute(
        serverId: String,
        attributeName: String,
    )

    fun getServersByType(gameType: String): List<Server>

    fun markServerForDeletion(serverId: String)
}
