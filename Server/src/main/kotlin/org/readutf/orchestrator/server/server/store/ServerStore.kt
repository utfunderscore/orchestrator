package org.readutf.orchestrator.server.server.store

import org.readutf.orchestrator.server.server.RegisteredServer
import org.readutf.orchestrator.shared.game.Game
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

    fun setGames(
        serverId: UUID,
        games: List<Game>,
    ): Server?

    fun getTimedOutServers(): List<RegisteredServer>

    fun getServerById(serverId: UUID): RegisteredServer?
}
