package org.readutf.orchestrator.shared.packets

import org.readutf.hermes.Packet
import org.readutf.orchestrator.shared.game.GameRequest

data class GameRequestPacket(
    val gameRequest: GameRequest,
) : Packet()
