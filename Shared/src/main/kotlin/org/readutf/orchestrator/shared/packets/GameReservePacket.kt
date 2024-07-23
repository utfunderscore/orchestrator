package org.readutf.orchestrator.shared.packets

import org.readutf.hermes.Packet
import java.util.UUID

data class GameReservePacket(
    val gameId: UUID,
) : Packet
