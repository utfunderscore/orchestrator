package org.readutf.orchestrator.server.server

import org.readutf.hermes.channel.ChannelUnregisterPacket
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
    }

    @PacketHandler
    fun onServerUnregister(packet: ServerUnregisterPacket) {
    }

    @PacketHandler
    fun onChannelClose(close: ChannelUnregisterPacket<*>) {
    }
}
