package org.readutf.orchestrator.shared.packets

import org.readutf.hermes.Packet
import java.util.*

data class ServerAttributeRemove(
    val serverId: UUID,
    val attributeName: String,
) : Packet()
