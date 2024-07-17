package org.readutf.orchestrator.server.server.store.listeners

import org.readutf.hermes.channel.HermesChannel
import org.readutf.hermes.listeners.TypedListener
import org.readutf.orchestrator.server.server.RegisteredServer
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.shared.packets.ServerRegisterPacket

class ServerRegisterListener(
    private val serverManager: ServerManager,
) : TypedListener<ServerRegisterPacket, HermesChannel> {
    override fun handle(
        packet: ServerRegisterPacket,
        channel: HermesChannel,
    ) {
        serverManager.registerServer(RegisteredServer.fromServer(packet.server, channel))
    }
}
