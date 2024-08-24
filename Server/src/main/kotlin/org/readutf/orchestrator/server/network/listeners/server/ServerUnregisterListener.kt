package org.readutf.orchestrator.server.network.listeners.server

import org.readutf.hermes.channel.HermesChannel
import org.readutf.orchestrator.server.network.listeners.NoopListener
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.shared.packets.ServerUnregisterPacket

class ServerUnregisterListener(
    private val serverManager: ServerManager,
) : NoopListener<ServerUnregisterPacket> {
    override fun handle(
        packet: ServerUnregisterPacket,
        channel: HermesChannel,
    ) {
        serverManager.unregisterServer(packet.serverId)
    }
}
