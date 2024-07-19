package org.readutf.orchestrator.server.network.listeners

import org.readutf.hermes.channel.HermesChannel
import org.readutf.orchestrator.server.server.RegisteredServer
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.shared.packets.ServerRegisterPacket

class ServerRegisterListener(
    private val serverManager: ServerManager,
) : Listener<ServerRegisterPacket> {
    override fun handle(
        packet: ServerRegisterPacket,
        channel: HermesChannel,
    ) {
        serverManager.registerServer(RegisteredServer.fromServer(packet.server, channel))
    }
}
