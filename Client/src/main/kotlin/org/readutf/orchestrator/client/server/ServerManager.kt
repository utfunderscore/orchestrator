package org.readutf.orchestrator.client.server

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.orchestrator.client.network.NetworkManager
import org.readutf.orchestrator.shared.game.GameFinderType
import org.readutf.orchestrator.shared.packets.ServerAttributeUpdate
import org.readutf.orchestrator.shared.packets.ServerHeartbeatPacket
import org.readutf.orchestrator.shared.packets.ServerRegisterPacket
import org.readutf.orchestrator.shared.packets.ServerUnregisterPacket
import org.readutf.orchestrator.shared.server.ServerAddress
import org.readutf.orchestrator.shared.server.ServerHeartbeat
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ServerManager(
    private val serverId: UUID,
    private val address: ServerAddress,
    private val gameTypes: List<String>,
    private val gameFinders: List<GameFinderType>,
    private val networkManager: NetworkManager,
) {
    private val logger = KotlinLogging.logger { }
    private val schedular = Executors.newSingleThreadScheduledExecutor()

    fun registerServer() {
        logger.info { "Registering server with orchestrator" }

        networkManager.sendPacket(
            ServerRegisterPacket(
                serverId = serverId,
                address = address,
                gameTypes = gameTypes,
                gameFinders = gameFinders,
            ),
        )
    }

    fun unregisterServer(serverId: UUID) {
        logger.info { "Unregistering server with orchestrator" }
        networkManager.sendPacket(
            ServerUnregisterPacket(
                serverId,
            ),
        )
    }

    fun sendHeartbeat(serverId: UUID) {
        logger.debug { "Sending heartbeat" }
        networkManager.sendPacket(
            ServerHeartbeatPacket(
                serverHeartbeat =
                    ServerHeartbeat(
                        serverId,
                    ),
            ),
        )
    }

    fun scheduleHeartbeat() {
        logger.info { "Scheduling heartbeat" }
        schedular.scheduleAtFixedRate(
            {
                sendHeartbeat(serverId)
            },
            0,
            5,
            TimeUnit.SECONDS,
        )
    }

    fun shutdown() {
        unregisterServer(serverId)
    }

    fun setAttribute(
        key: String,
        value: Any,
    ) {
        networkManager.sendPacket(
            ServerAttributeUpdate(
                serverId,
                key,
                value,
            ),
        )
    }
}
