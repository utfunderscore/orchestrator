package org.readutf.orchestrator.shared.packet.serializer

import org.readutf.orchestrator.shared.packet.Packet

interface PacketSerializer {
    fun serialize(packet: Packet): ByteArray

    fun deserialize(bytes: ByteArray): Packet
}
