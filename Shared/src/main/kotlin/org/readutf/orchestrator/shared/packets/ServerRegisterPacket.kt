package org.readutf.orchestrator.shared.packets

import org.readutf.hermes.Packet
import org.readutf.orchestrator.shared.server.Server

data class ServerRegisterPacket(
    val server: Server,
) : Packet {
    override fun toString(): String = "ServerRegisterPacket(server=$server)"
}
