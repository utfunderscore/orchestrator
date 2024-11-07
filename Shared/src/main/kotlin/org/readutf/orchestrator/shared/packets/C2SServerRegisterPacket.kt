package org.readutf.orchestrator.shared.packets

import org.readutf.hermes.Packet
import org.readutf.orchestrator.shared.server.Server
import org.readutf.orchestrator.shared.server.ServerAddress

data class ServerRegisterPacket(
    val serverId: String,
    val serverType: String,
    val address: ServerAddress,
    val protocolVersion: Byte = Server.PROTOCOL_ID,
) : Packet()

enum class ServerRegisterResponse {
    SUCCESS,
    INVALID_PROTOCOL,
    UNKNOWN_REASON,
}
