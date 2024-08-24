package org.readutf.orchestrator.shared.packets

import org.readutf.hermes.Packet
import java.util.*

data class ServerAttributeUpdate(
    val serverId: UUID,
    val attributeName: String,
    val attribute: Any,
) : Packet()
