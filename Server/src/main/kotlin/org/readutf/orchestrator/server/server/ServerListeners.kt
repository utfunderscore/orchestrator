package org.readutf.orchestrator.server.server

import org.readutf.hermes.listeners.annotation.PacketHandler
import org.readutf.hermes.platform.netty.NettyHermesChannel
import org.readutf.orchestrator.shared.packets.ServerRegisterPacket
import org.readutf.orchestrator.shared.packets.ServerUnregisterPacket

class ServerListeners(
    private val serverManager: ServerManager,
) {
    @PacketHandler
    fun onServerRegister(
        packet: ServerRegisterPacket,
        channel: NettyHermesChannel,
    ) {
        try {
            serverManager.registerServer(RegisteredServer.fromServer(packet.server, channel))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @PacketHandler
    fun onServerUnregister(packet: ServerUnregisterPacket) {
        serverManager.unRegisterServer(packet.serverId)
    }
}
