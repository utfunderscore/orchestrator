package org.readutf.orchestrator.server.server.listeners

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import org.readutf.hermes.channel.HermesChannel
import org.readutf.hermes.listeners.TypedListener
import org.readutf.orchestrator.common.packets.C2SHeartbeatPacket
import org.readutf.orchestrator.server.server.ServerManager

class ServerHeartbeatListener(
    private val serverManager: ServerManager,
) : TypedListener<C2SHeartbeatPacket, HermesChannel, Unit> {
    override fun handle(
        packet: C2SHeartbeatPacket,
        channel: HermesChannel,
    ): Result<Unit, Throwable> {
        serverManager.handleHeartbeat(packet.serverId, packet.heartbeat)
        return Ok(Unit)
    }
}
