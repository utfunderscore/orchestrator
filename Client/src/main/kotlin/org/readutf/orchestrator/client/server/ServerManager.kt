package org.readutf.orchestrator.client.server

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.orchestrator.client.network.NetworkManager
import org.readutf.orchestrator.shared.packets.*
import org.readutf.orchestrator.shared.server.ServerAddress
import org.readutf.orchestrator.shared.server.ServerHeartbeat
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ServerManager(
    private val serverId: String,
    private val address: ServerAddress,
    private val serverType: String,
    private val networkManager: NetworkManager,
) {
    private val logger = KotlinLogging.logger { }
    private val schedular = Executors.newSingleThreadScheduledExecutor()

    init {
        val registerResult = registerServer().join()

        if (registerResult != ServerRegisterResponse.SUCCESS) {
            logger.error { "Failed to register server with orchestrator" }
            throw IllegalStateException("Failed to register server with orchestrator")
        }

        scheduleHeartbeat()
    }

    /**
     * Register the server with the orchestrator
     * If the server registers successfully, the result of the future will be true
     * @return CompletableFuture<Boolean>
     */
    private fun registerServer(): CompletableFuture<ServerRegisterResponse> {
        logger.info { "Registering server with orchestrator" }

        return networkManager
            .sendPacketWithResponse<ServerRegisterResponse>(
                ServerRegisterPacket(
                    serverId = serverId,
                    serverType = serverType,
                    address = address,
                ),
            )
    }

    private fun unregisterServer(serverId: String) {
        logger.info { "Unregistering server with orchestrator" }
        networkManager.sendPacket(
            C2SServerUnregisterPacket(
                serverId,
            ),
        )
    }

    private fun sendHeartbeat(serverId: String) {
        logger.debug { "Sending heartbeat" }
        networkManager.sendPacket(
            C2SServerHeartbeatPacket(
                serverHeartbeat =
                    ServerHeartbeat(
                        serverId,
                    ),
            ),
        )
    }

    private fun scheduleHeartbeat() {
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
            C2SServerAttributeUpdate(
                serverId,
                key,
                value,
            ),
        )
    }
}
