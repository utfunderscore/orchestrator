package org.readutf.orchestrator.shared.packets

import org.readutf.hermes.Packet
import java.util.UUID

class ServerUnregisterPacket(
    val serverId: UUID,
) : Packet {
    override fun toString(): String = "ServerUnregisterPacket(serverId=$serverId)"
}
