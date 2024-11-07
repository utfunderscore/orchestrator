package org.readutf.orchestrator.server.server

import com.fasterxml.jackson.annotation.JsonIgnore
import org.readutf.hermes.channel.HermesChannel
import org.readutf.orchestrator.shared.server.Server
import org.readutf.orchestrator.shared.server.ServerAddress
import org.readutf.orchestrator.shared.server.ServerHeartbeat

class RegisteredServer(
    serverId: String, // Container ID
    serverType: String,
    address: ServerAddress,
    heartbeat: ServerHeartbeat = ServerHeartbeat(serverId, System.currentTimeMillis()),
    channel: HermesChannel,
) : Server(
        serverId = serverId,
        address = address,
        serverType = serverType,
        heartbeat = heartbeat,
        attributes = mutableMapOf(),
    ) {
    @JsonIgnore
    val channel = channel

    companion object {
        fun create(
            server: Server,
            hermesChannel: HermesChannel,
        ): RegisteredServer =
            RegisteredServer(
                serverId = server.serverId,
                address = server.address,
                serverType = server.serverType,
                heartbeat = server.heartbeat,
                channel = hermesChannel,
            )
    }
}
