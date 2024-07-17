package org.readutf.orchestrator.shared.server

import org.readutf.orchestrator.shared.attribute.TypedAttribute
import java.util.UUID

open class Server(
    val serverId: UUID,
    val address: ServerAddress,
    val supportedModes: List<String>,
    var heartbeat: ServerHeartbeat = ServerHeartbeat(serverId, System.currentTimeMillis()),
    val attributes: MutableMap<String, TypedAttribute<*>> = mutableMapOf(),
) {
    override fun toString(): String = "Server(serverId=$serverId, address=$address, supportedModes=$supportedModes)"
}
