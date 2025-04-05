package org.readutf.orchestrator.common.packets

import com.fasterxml.jackson.databind.JsonNode
import org.readutf.hermes.Packet
import java.util.UUID

data class C2SRenewPacket(
    val serverId: UUID,
    val containerId: String,
    val attributes: Map<String, JsonNode>,
) : Packet<Boolean>()
