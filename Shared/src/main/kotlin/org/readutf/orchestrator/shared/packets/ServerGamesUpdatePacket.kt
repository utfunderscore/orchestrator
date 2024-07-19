package org.readutf.orchestrator.shared.packets

import org.readutf.hermes.Packet
import org.readutf.orchestrator.shared.game.Game
import java.util.UUID

data class ServerGamesUpdatePacket(
    val serverId: UUID,
    val games: List<Game>,
) : Packet
