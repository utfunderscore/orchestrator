package org.readutf.orchestrator.server.server.listeners

import org.readutf.hermes.channel.HermesChannel
import org.readutf.orchestrator.server.network.listeners.NoopListener
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.shared.packets.C2SServerUnregisterPacket

class ServerUnregisterListener(
    private val serverManager: ServerManager,
) : NoopListener<C2SServerUnregisterPacket> {
    override fun handle(
        packet: C2SServerUnregisterPacket,
        channel: HermesChannel,
    ) {
        serverManager.unregisterServer(packet.serverId)
    }
}
