package org.readutf.orchestrator.server.server

import org.readutf.hermes.channel.HermesChannel
import org.readutf.orchestrator.shared.game.Game
import org.readutf.orchestrator.shared.game.GameFinderType
import org.readutf.orchestrator.shared.server.Server
import org.readutf.orchestrator.shared.server.ServerAddress
import org.readutf.orchestrator.shared.server.ServerHeartbeat
import java.util.*

class RegisteredServer(
    serverId: UUID,
    address: ServerAddress,
    gameTypes: List<String>,
    gameFinders: List<GameFinderType>,
    heartbeat: ServerHeartbeat = ServerHeartbeat(serverId, System.currentTimeMillis()),
    activeGames: MutableList<Game>,
    val channel: HermesChannel,
) : Server(serverId, address, gameTypes, gameFinders, heartbeat, activeGames) {
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
                server.activeGames,
                hermesChannel,
            )
    }
}
