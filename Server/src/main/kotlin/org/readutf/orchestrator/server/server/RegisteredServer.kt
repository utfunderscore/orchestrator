package org.readutf.orchestrator.server.server

import com.fasterxml.jackson.annotation.JsonIgnore
import org.readutf.hermes.channel.HermesChannel
import org.readutf.orchestrator.server.utils.TimeUtils
import org.readutf.orchestrator.shared.game.GameFinderType
import org.readutf.orchestrator.shared.server.Server
import org.readutf.orchestrator.shared.server.ServerAddress
import org.readutf.orchestrator.shared.server.ServerHeartbeat
import java.util.UUID

class RegisteredServer(
    serverId: UUID,
    address: ServerAddress,
    gameTypes: List<String>,
    gameFinders: List<GameFinderType>,
    heartbeat: ServerHeartbeat = ServerHeartbeat(serverId, System.currentTimeMillis()),
    channel: HermesChannel,
    private val registeredAt: Long = System.currentTimeMillis(),
) : Server(serverId, address, gameTypes, gameFinders, heartbeat, mutableMapOf()) {
    @JsonIgnore
    fun getUptime() = System.currentTimeMillis() - registeredAt

    @JsonIgnore
    val channel = channel

    @JsonIgnore
    fun getUptimeString(): String = TimeUtils.formatDuration(getUptime())

    companion object {
        fun create(
            server: Server,
            hermesChannel: HermesChannel,
        ): RegisteredServer =
            RegisteredServer(
                server.serverId,
                server.address,
                server.gameTypes,
                server.gameFinders,
                server.heartbeat,
                hermesChannel,
            )
    }
}
