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
import java.util.concurrent.CompletableFuture
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

    /**
     * Register the server with the orchestrator
     * If the server registers successfully, the result of the future will be true
     * @return CompletableFuture<Boolean>
     */
    fun registerServer(): CompletableFuture<Boolean> {
        logger.info { "Registering server with orchestrator" }

        return networkManager
            .sendPacketWithResponse<Boolean>(
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
