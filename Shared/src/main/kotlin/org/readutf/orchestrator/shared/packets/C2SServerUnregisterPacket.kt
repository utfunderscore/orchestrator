package org.readutf.orchestrator.shared.packets

import org.readutf.hermes.Packet

class C2SServerUnregisterPacket(
    val serverId: String,
) : Packet() {
    override fun toString(): String = "ServerUnregisterPacket(serverId=$serverId)"
}
