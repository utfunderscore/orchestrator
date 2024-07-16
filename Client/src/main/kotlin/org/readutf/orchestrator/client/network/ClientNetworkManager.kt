package org.readutf.orchestrator.client.network

import com.esotericsoftware.kryo.kryo5.Kryo
import org.readutf.hermes.Packet
import org.readutf.hermes.PacketManager
import org.readutf.hermes.platform.netty.nettyClient
import org.readutf.hermes.serializer.KryoPacketSerializer
import org.readutf.orchestrator.shared.packets.ServerRegisterPacket
import org.readutf.orchestrator.shared.packets.ServerUnregisterPacket
import org.readutf.orchestrator.shared.server.Server
import java.util.UUID

class ClientNetworkManager(
    kryo: Kryo,
) {
    private var packetManager =
        PacketManager
            .nettyClient(
                hostName = "localhost",
                port = 2980,
                serializer = KryoPacketSerializer(kryo),
            ).start()

    fun sendPacket(packet: Packet) {
        packetManager.sendPacket(packet)
    }

    fun registerServer(server: Server) {
        packetManager.sendPacket(ServerRegisterPacket(server))
    }

    fun unregisterServer(serverId: UUID) {
        packetManager.sendPacket(ServerUnregisterPacket(serverId))
    }
}
