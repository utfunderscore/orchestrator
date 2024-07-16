package org.readutf.orchestrator.server.server

import org.readutf.hermes.platform.netty.NettyHermesChannel
import org.readutf.orchestrator.shared.server.Server
import org.readutf.orchestrator.shared.server.ServerAddress
import java.util.*

class RegisteredServer(
    val channel: NettyHermesChannel,
    serverId: UUID,
    address: ServerAddress,
    supportedModes: List<String>,
    var lastHeartbeat: Long = System.currentTimeMillis(),
) : Server(
        serverId,
        address,
        supportedModes,
    ) {
    fun heartbeat() {
        lastHeartbeat = System.currentTimeMillis()
    }

    companion object {
        fun fromServer(
            server: Server,
            channel: NettyHermesChannel,
        ): RegisteredServer =
            RegisteredServer(
                channel,
                server.serverId,
                server.address,
                server.supportedModes,
            )
    }
}
