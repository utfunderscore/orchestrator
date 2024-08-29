package org.readutf.orchestrator.shared.packets

import org.readutf.hermes.Packet
import org.readutf.orchestrator.shared.game.GameFinderType
import org.readutf.orchestrator.shared.server.ServerAddress
import java.util.*

data class ServerRegisterPacket(
    val serverId: UUID,
    val address: ServerAddress,
    val gameTypes: List<String>,
    val gameFinders: List<GameFinderType>,
) : Packet()
