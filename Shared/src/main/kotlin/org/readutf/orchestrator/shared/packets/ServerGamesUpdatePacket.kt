package org.readutf.orchestrator.shared.packets

import org.readutf.hermes.Packet
import org.readutf.orchestrator.shared.game.Game

data class ServerGamesUpdatePacket(
    val serverId: String,
    val games: List<Game>,
) : Packet()
