package org.readutf.orchestrator.server.server

import org.readutf.hermes.channel.HermesChannel
import org.readutf.orchestrator.shared.server.Server
import org.readutf.orchestrator.shared.server.ServerAddress
import java.util.UUID

class RegisteredServer(
    val channel: HermesChannel,
    serverId: UUID,
    address: ServerAddress,
    supportedModes: List<String>,
) : Server(
        serverId = serverId,
        address = address,
        supportedModes = supportedModes,
        activeGames = mutableListOf(),
    ) {
    companion object {
        fun fromServer(
            server: Server,
            channel: HermesChannel,
        ): RegisteredServer =
            RegisteredServer(
                channel,
                server.serverId,
                server.address,
                server.supportedModes,
            )
    }
}
