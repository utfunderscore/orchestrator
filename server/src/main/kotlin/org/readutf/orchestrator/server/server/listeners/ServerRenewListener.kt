package org.readutf.orchestrator.server.server.listeners

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import org.readutf.hermes.channel.HermesChannel
import org.readutf.hermes.listeners.TypedListener
import org.readutf.orchestrator.common.packets.C2SRenewPacket
import org.readutf.orchestrator.common.server.ShortContainerId
import org.readutf.orchestrator.server.server.ServerManager

class ServerRenewListener(
    val serverManager: ServerManager,
) : TypedListener<C2SRenewPacket, HermesChannel, Boolean> {
    override fun handle(
        packet: C2SRenewPacket,
        channel: HermesChannel,
    ): Result<Boolean, Throwable> {
        serverManager.renewServer(packet.serverId, ShortContainerId.of(packet.containerId), channel, packet.attributes.toMutableMap())
        return Ok(true)
    }
}
