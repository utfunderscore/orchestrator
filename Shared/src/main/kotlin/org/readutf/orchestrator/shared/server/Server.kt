package org.readutf.orchestrator.shared.server

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo.As
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id
import org.readutf.orchestrator.shared.game.GameFinderType
import java.util.UUID

open class Server(
    val serverId: UUID,
    val address: ServerAddress,
    val gameTypes: List<String>,
    val gameFinders: List<GameFinderType>,
    var heartbeat: ServerHeartbeat = ServerHeartbeat(serverId, System.currentTimeMillis()),
    @JsonTypeInfo(include = As.WRAPPER_OBJECT, use = Id.CLASS) var attributes: MutableMap<String, Any>,
) {
    override fun toString(): String =
        "Server(serverId=$serverId, address=$address, gameTypes=$gameTypes, gameFinders=$gameFinders, heartbeat=$heartbeat)"

    fun getShortId(): String = serverId.toString().substring(0, 8)
}
