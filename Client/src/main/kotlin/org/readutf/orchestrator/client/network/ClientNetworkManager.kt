package org.readutf.orchestrator.client.network

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.hermes.Packet
import org.readutf.hermes.PacketManager
import java.util.UUID

class ClientNetworkManager(
    private val packetManager: PacketManager<*>,
    private val serverId: UUID,
) {
    private var logger = KotlinLogging.logger { }

    fun sendPacket(packet: Packet) {
        packetManager.sendPacket(packet)
    }

    fun shutdown() {
        packetManager.stop()
    }
}
