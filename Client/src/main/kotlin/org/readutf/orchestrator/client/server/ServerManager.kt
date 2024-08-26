package org.readutf.orchestrator.client.server

import org.readutf.orchestrator.client.network.ClientNetworkManager
import org.readutf.orchestrator.shared.game.GameFinderType
import org.readutf.orchestrator.shared.packets.*
import org.readutf.orchestrator.shared.server.Server
import org.readutf.orchestrator.shared.server.ServerAddress
import org.readutf.orchestrator.shared.server.ServerHeartbeat
import java.util.UUID
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class ServerManager(
    private val serverId: UUID,
    private val serverAddress: ServerAddress,
    private val supportedGameTypes: MutableList<String>,
    private val gameFinderTypes: MutableList<GameFinderType>,
    private val networkManager: ClientNetworkManager,
    scheduledExecutor: ScheduledExecutorService,
) {
    init {
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
                    mutableMapOf(),
                ),
            ),
        )
    }

    private fun sendHeartbeat() {
        networkManager.sendPacket(
            ServerHeartbeatPacket(ServerHeartbeat(serverId = serverId)),
        )
    }

    fun setAttribute(
        key: String,
        any: Any,
    ) {
        networkManager.sendPacket(ServerAttributeUpdate(serverId, key, any))
    }

    fun removeAttribute(key: String) {
        networkManager.sendPacket(ServerAttributeRemove(serverId, key))
    }

    fun shutdown() {
        networkManager.sendPacket(
            ServerUnregisterPacket(serverId),
        )
    }
}
