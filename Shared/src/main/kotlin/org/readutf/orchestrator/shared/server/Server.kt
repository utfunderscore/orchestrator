package org.readutf.orchestrator.shared.server

import org.readutf.orchestrator.shared.game.GameFinderType
import org.readutf.orchestrator.shared.utils.TypedObject
import java.util.UUID

open class Server(
    val serverId: UUID,
    val address: ServerAddress,
    val gameTypes: List<String>,
    val gameFinders: List<GameFinderType>,
    var heartbeat: ServerHeartbeat = ServerHeartbeat(serverId, System.currentTimeMillis()),
    var attributes: MutableMap<String, TypedObject>,
) {
    override fun toString(): String =
        "Server(serverId=$serverId, address=$address, gameTypes=$gameTypes, gameFinders=$gameFinders, heartbeat=$heartbeat)"

    fun getShortId(): String = serverId.toString().substring(0, 8)
}
