package org.readutf.orchestrator.server.server.listeners

import org.readutf.hermes.channel.HermesChannel
import org.readutf.orchestrator.server.network.listeners.NoopListener
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.shared.packets.C2SUnregisterPacket

class ServerUnregisterListener(
    private val serverManager: ServerManager,
) : NoopListener<C2SUnregisterPacket> {
    override fun handle(
        packet: C2SUnregisterPacket,
        channel: HermesChannel,
    ) {
        serverManager.unregisterServer(packet.serverId)
    }
}
