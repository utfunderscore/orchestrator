package org.readutf.orchestrator.server.server

import com.alibaba.fastjson2.annotation.JSONField
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
) : Server(serverId, address, gameTypes, gameFinders, heartbeat) {
    @JSONField(serialize = false)
    fun getUptime() = System.currentTimeMillis() - registeredAt

    @JSONField(serialize = false)
    val channel = channel

    @JSONField(serialize = false)
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
