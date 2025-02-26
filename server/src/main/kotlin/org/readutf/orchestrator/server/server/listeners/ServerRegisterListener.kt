package org.readutf.orchestrator.server.server.listeners

import com.github.michaelbull.result.get
import org.readutf.hermes.channel.HermesChannel
import org.readutf.hermes.listeners.TypedListener
import org.readutf.orchestrator.common.packets.C2SRegisterPacket
import org.readutf.orchestrator.server.server.ServerManager
import java.util.UUID

class ServerRegisterListener(
    private val serverManager: ServerManager,
) : TypedListener<C2SRegisterPacket, HermesChannel, UUID?> {
    override fun handle(
        packet: C2SRegisterPacket,
        channel: HermesChannel,
    ): UUID? {
        val server = serverManager.registerServer(packet.containerId, channel, packet.attributes)
        return server.get()?.serverId
    }
}
