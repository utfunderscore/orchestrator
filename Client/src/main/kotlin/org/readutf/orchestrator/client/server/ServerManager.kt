package org.readutf.orchestrator.client.server

import org.readutf.orchestrator.client.network.ClientNetworkManager
import org.readutf.orchestrator.shared.game.GameFinderType
import org.readutf.orchestrator.shared.packets.ServerHeartbeatPacket
import org.readutf.orchestrator.shared.packets.ServerRegisterPacket
import org.readutf.orchestrator.shared.packets.ServerUnregisterPacket
import org.readutf.orchestrator.shared.server.Server
import org.readutf.orchestrator.shared.server.ServerAddress
import org.readutf.orchestrator.shared.server.ServerHeartbeat
import java.util.UUID
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class ServerManager(
    val serverId: UUID,
    val serverAddress: ServerAddress,
    val supportedGameTypes: List<String>,
    val gameFinderTypes: MutableList<GameFinderType>,
    val networkManager: ClientNetworkManager,
    scheduledExecutor: ScheduledExecutorService,
) {
    init {
        registerServer()

        scheduledExecutor.scheduleAtFixedRate(
            { sendHeartbeat() },
            1,
            1,
            TimeUnit.SECONDS,
        )
    }

    fun registerServer() {
        networkManager.sendPacket(
            ServerRegisterPacket(
                Server(
                    serverId = serverId,
                    address = serverAddress,
                    gameTypes = supportedGameTypes,
                    gameFinders = gameFinderTypes,
                    heartbeat = ServerHeartbeat(serverId = serverId),
                    activeGames = mutableListOf(),
                ),
            ),
        )
    }

    fun sendHeartbeat() {
        networkManager.sendPacket(
            ServerHeartbeatPacket(ServerHeartbeat(serverId = serverId)),
        )
    }

    fun shutdown() {
        networkManager.sendPacket(
            ServerUnregisterPacket(serverId),
        )
    }
}