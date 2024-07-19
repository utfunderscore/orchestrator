package org.readutf.orchestrator.client.network

import com.esotericsoftware.kryo.kryo5.Kryo
import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.hermes.Packet
import org.readutf.hermes.PacketManager
import org.readutf.hermes.platform.netty.nettyClient
import org.readutf.hermes.serializer.KryoPacketSerializer
import org.readutf.orchestrator.shared.game.Game
import org.readutf.orchestrator.shared.packets.ServerGamesUpdatePacket
import org.readutf.orchestrator.shared.packets.ServerHeartbeatPacket
import org.readutf.orchestrator.shared.packets.ServerRegisterPacket
import org.readutf.orchestrator.shared.packets.ServerUnregisterPacket
import org.readutf.orchestrator.shared.server.Server
import org.readutf.orchestrator.shared.server.ServerHeartbeat
import java.util.UUID

class ClientNetworkManager(
    kryo: Kryo,
    private val serverId: UUID,
) {
    private var logger = KotlinLogging.logger { }

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
        packetManager.sendPacket(
            ServerRegisterPacket(server),
        )
    }

    fun unregisterServer(serverId: UUID) {
        packetManager.sendPacket(
            ServerUnregisterPacket(serverId),
        )
    }

    fun sendHeartbeat() {
        logger.debug { "Sending heartbeat" }

        packetManager.sendPacket(
            ServerHeartbeatPacket(ServerHeartbeat(serverId, System.currentTimeMillis())),
        )
    }

    fun updateGames(games: List<Game>) {
        logger.info { "Updating active games for server" }

        packetManager.sendPacket(
            ServerGamesUpdatePacket(serverId = serverId, games = games)
        )
    }

    fun shutdown() {
        unregisterServer(serverId)
        packetManager.stop()
    }
}
