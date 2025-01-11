package org.readutf.orchestrator.edge.network.listener

import net.minestom.server.network.packet.client.ClientPacket
import org.readutf.orchestrator.edge.network.ClientConnection

fun interface PacketListener<T : ClientPacket> {
    fun onPacket(
        packet: T,
        clientConnection: ClientConnection,
    )
}
