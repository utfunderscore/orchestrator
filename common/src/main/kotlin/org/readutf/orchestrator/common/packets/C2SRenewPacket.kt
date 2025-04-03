package org.readutf.orchestrator.common.packets

import java.util.UUID
import org.readutf.hermes.Packet

data class C2SRenewPacket(
    val containerId: String,
    val attributes: Map<String, Any>,
) : Packet<UUID>() {
}