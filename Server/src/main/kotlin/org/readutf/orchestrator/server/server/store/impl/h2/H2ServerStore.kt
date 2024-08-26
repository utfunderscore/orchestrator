package org.readutf.orchestrator.server.server.store.impl.h2

import org.readutf.orchestrator.server.server.RegisteredServer
import org.readutf.orchestrator.server.server.store.ServerStore
import org.readutf.orchestrator.shared.server.Server
import org.readutf.orchestrator.shared.server.ServerHeartbeat
import java.util.*

class H2ServerStore : ServerStore {
    override fun saveServer(registeredServer: RegisteredServer) {
        TODO("Not yet implemented")
    }

    override fun removeServer(serverId: UUID) {
        TODO("Not yet implemented")
    }

    override fun getServersByChannel(channelId: String): List<RegisteredServer> {
        TODO("Not yet implemented")
    }

    override fun updateHeartbeat(
        serverId: UUID,
        serverHeartbeat: ServerHeartbeat,
    ) {
        TODO("Not yet implemented")
    }

    override fun getTimedOutServers(): List<RegisteredServer> {
        TODO("Not yet implemented")
    }

    override fun getServerById(serverId: UUID): RegisteredServer? {
        TODO("Not yet implemented")
    }

    override fun getAllServers(): List<RegisteredServer> {
        TODO("Not yet implemented")
    }

    override fun getServerByShortId(shortId: String): Server? {
        TODO("Not yet implemented")
    }

    override fun setAttribute(
        serverId: UUID,
        attributeName: String,
        data: Any,
    ) {
        TODO("Not yet implemented")
    }

    override fun removeAttribute(
        serverId: UUID,
        attributeName: String,
    ) {
        TODO("Not yet implemented")
    }

    override fun getServersByType(gameType: String): List<Server> {
        TODO("Not yet implemented")
    }
}
