package org.readutf.orchestrator.shared.server

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.readutf.orchestrator.shared.game.GameFinderType
import org.readutf.orchestrator.shared.utils.TypeWrapper
import java.util.UUID

open class Server(
    @JsonProperty("serverId") val serverId: UUID,
    @JsonProperty("address") val address: ServerAddress,
    @JsonProperty("gameTypes") val gameTypes: List<String>,
    @JsonProperty("gameFinders") val gameFinders: List<GameFinderType>,
    @JsonProperty("heartbeat") var heartbeat: ServerHeartbeat = ServerHeartbeat(serverId, System.currentTimeMillis()),
    @JsonProperty("attributes") var attributes: MutableMap<String, TypeWrapper> = mutableMapOf(),
) {
    @JsonIgnore
    fun getShortId(): String = serverId.toString().substring(0, 8)

    override fun toString(): String =
        "Server(serverId=$serverId, address=$address, gameTypes=$gameTypes, gameFinders=$gameFinders, heartbeat=$heartbeat, attributes=$attributes)"
}
