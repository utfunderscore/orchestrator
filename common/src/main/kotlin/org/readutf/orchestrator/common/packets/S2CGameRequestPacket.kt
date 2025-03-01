package org.readutf.orchestrator.common.packets

import org.readutf.hermes.Packet
import java.util.UUID

data class S2CGameRequestPacket(
    val gameType: String,
    val players: List<UUID>,
) : Packet<UUID>()
