package org.readutf.orchestrator.server.network.listeners

import org.readutf.hermes.channel.HermesChannel
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.shared.packets.ServerUnregisterPacket

class ServerUnregisterListener(
    private val serverManager: ServerManager,
) : Listener<ServerUnregisterPacket, Unit> {
    override fun handle(
        packet: ServerUnregisterPacket,
        channel: HermesChannel,
    ) {
        serverManager.unregisterServer(packet.serverId)
    }
}
