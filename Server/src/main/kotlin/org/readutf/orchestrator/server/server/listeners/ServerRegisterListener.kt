package org.readutf.orchestrator.server.server.listeners

import org.readutf.hermes.channel.HermesChannel
import org.readutf.orchestrator.server.network.listeners.Listener
import org.readutf.orchestrator.server.server.RegisteredServer
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.shared.packets.ServerRegisterPacket
import org.readutf.orchestrator.shared.server.Server

class ServerRegisterListener(
    private val serverManager: ServerManager,
) : Listener<ServerRegisterPacket, Boolean> {
    override fun handle(
        packet: ServerRegisterPacket,
        channel: HermesChannel,
    ): Boolean {
        val server = Server(packet.serverId, packet.address, packet.gameTypes, packet.gameFinders)

        try {
            serverManager.registerServer(
                RegisteredServer.create(
                    server,
                    channel,
                ),
            )
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}
