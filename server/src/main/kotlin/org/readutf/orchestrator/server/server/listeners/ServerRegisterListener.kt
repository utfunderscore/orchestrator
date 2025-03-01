package org.readutf.orchestrator.server.server.listeners

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import org.readutf.hermes.channel.HermesChannel
import org.readutf.hermes.listeners.TypedListener
import org.readutf.orchestrator.common.packets.C2SRegisterPacket
import org.readutf.orchestrator.server.server.ServerManager
import java.util.UUID

class ServerRegisterListener(
    private val serverManager: ServerManager,
) : TypedListener<C2SRegisterPacket, HermesChannel, UUID> {
    override fun handle(
        packet: C2SRegisterPacket,
        channel: HermesChannel,
    ): Result<UUID, Throwable> = serverManager.registerServer(packet.containerId, channel, packet.attributes).map { it.id }
}
