package org.readutf.orchestrator.shared.server

import java.util.UUID

open class Server(
    val serverId: UUID,
    val address: ServerAddress,
    val supportedModes: List<String>,
) {
    override fun toString(): String = "Server(serverId=$serverId, address=$address, supportedModes=$supportedModes)"
}
