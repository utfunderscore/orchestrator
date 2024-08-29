package org.readutf.orchestrator.client.network

import org.readutf.hermes.Packet
import org.readutf.hermes.PacketManager
import org.readutf.hermes.channel.ChannelClosePacket
import org.readutf.hermes.channel.HermesChannel
import org.readutf.hermes.listeners.Listener
import org.readutf.hermes.platform.netty.nettyClient
import org.readutf.hermes.serializer.KryoPacketSerializer
import org.readutf.orchestrator.client.OrchestratorClient
import org.readutf.orchestrator.shared.kryo.KryoCreator
import java.net.SocketException

class NetworkManager(
    val client: OrchestratorClient,
) {
    val packetManager = createPacketManager()

    private fun createPacketManager() =
        PacketManager
            .nettyClient(
                client.orchestratorHost,
                client.orchestratorPort,
                KryoPacketSerializer(KryoCreator.build()),
            ).editListeners {
                it.registerListener(
                    ChannelClosePacket::class.java,
                    object : Listener {
                        override fun acceptPacket(
                            hermesChannel: HermesChannel,
                            packet: Packet,
                        ) {
                            client.onDisconnect()
                        }
                    },
                )
            }.exception {
                println("error")
            }.exception(SocketException::class.java) {
                client.onDisconnect()
            }.start()

    fun shutdown() {
        packetManager.stop()
    }

    fun sendPacket(packet: Packet) {
        try {
            packetManager.sendPacket(packet)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
