package org.readutf.orchestrator.shared.server

import org.readutf.orchestrator.shared.attribute.TypedAttribute
import org.readutf.orchestrator.shared.game.Game
import org.readutf.orchestrator.shared.game.GameFinderType
import java.util.UUID

open class Server(
    val serverId: UUID,
    val address: ServerAddress,
    val gameTypes: List<String>,
    val gameFinders: List<GameFinderType>,
    var heartbeat: ServerHeartbeat = ServerHeartbeat(serverId, System.currentTimeMillis()),
    val activeGames: MutableList<Game>,
    val attributes: MutableMap<String, TypedAttribute<*>> = mutableMapOf(),
) {
    override fun toString(): String = "Server(serverId=$serverId, address=$address, supportedModes=$gameTypes)"
}
