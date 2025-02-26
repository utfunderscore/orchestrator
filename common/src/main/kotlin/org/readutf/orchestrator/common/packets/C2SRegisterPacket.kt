package org.readutf.orchestrator.common.packets

import com.fasterxml.jackson.databind.JsonNode
import org.readutf.hermes.Packet

data class C2SRegisterPacket(
    val containerId: String,
    val attributes: Map<String, JsonNode>,
) : Packet()
