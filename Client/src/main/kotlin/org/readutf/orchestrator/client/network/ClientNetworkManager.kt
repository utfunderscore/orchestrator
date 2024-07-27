package org.readutf.orchestrator.client.network

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.hermes.Packet
import org.readutf.hermes.PacketManager
import org.readutf.hermes.channel.HermesChannel
import org.readutf.hermes.listeners.TypedListener
import java.util.UUID

class ClientNetworkManager(
    val packetManager: PacketManager<*>,
    private val serverId: UUID,
) {
    private var logger = KotlinLogging.logger { }

    fun sendPacket(packet: Packet) {
        packetManager.sendPacket(packet)
    }

    fun shutdown() {
        packetManager.stop()
    }

    inline fun <reified T : Packet, reified U : HermesChannel, V> registerListener(listener: TypedListener<T, U, V>) {
        packetManager.editListeners {
            it.registerListener<T, U, V>(listener)
        }
    }
}
