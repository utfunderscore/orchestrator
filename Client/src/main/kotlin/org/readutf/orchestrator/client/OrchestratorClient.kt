package org.readutf.orchestrator.client

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.orchestrator.client.network.NetworkManager
import org.readutf.orchestrator.client.server.ServerManager
import org.readutf.orchestrator.shared.packets.C2SServerUnregisterPacket
import org.readutf.orchestrator.shared.server.ServerAddress
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class OrchestratorClient(
    val serverId: String,
    val orchestratorHost: String,
    val orchestratorPort: Int,
    val serverAddress: ServerAddress,
    val serverType: String,
    private val autoReconnect: Boolean = true,
    private val maxReconnectAttempts: Int = 15,
    private var shutdownRequestHandler: () -> Unit,
    private val onConnect: ServerManager.() -> Unit,
) {
    private val logger = KotlinLogging.logger { }

    var networkManager: NetworkManager? = null
    private var serverManager: ServerManager? = null

    private val reconnectScheduler = Executors.newSingleThreadScheduledExecutor()
    private var shuttingDown = false
    private var reconnectAttempts = 0

    fun connect() {
        logger.info { "Connecting to orchestrator... ($orchestratorHost:$orchestratorPort)" }
        try {
            networkManager =
                NetworkManager(
                    orchestratorHost = orchestratorHost,
                    orchestratorPort = orchestratorPort,
                    onDisconnect = ::onDisconnect,
                    shutdownHandler = shutdownRequestHandler,
                )
            serverManager = ServerManager(serverId, serverAddress, serverType, networkManager!!)
            val executor = Executors.newSingleThreadScheduledExecutor()
            executor.schedule({
                if (serverManager != null) {
                    onConnect(serverManager!!)
                }
            }, 10, TimeUnit.MILLISECONDS)
            reconnectAttempts = 0
        } catch (e: Throwable) {
            logger.error(e) { "Failed to connect to orchestrator" }
            onDisconnect()
        }
    }

    fun setAttribute(
        key: String,
        value: Any,
    ) {
        serverManager?.setAttribute(key, value)
    }

    fun onDisconnect() {
        if (!autoReconnect || reconnectAttempts >= maxReconnectAttempts) {
            disconnect()
            return
        }
        if (shuttingDown) return
        serverManager?.shutdown()
        networkManager?.shutdown()

        scheduleReconnect()
        reconnectAttempts++
    }

    private fun scheduleReconnect() {
        println("Reconnecting in 5 seconds (attempts: $reconnectAttempts)")
        reconnectScheduler.schedule(::connect, 5, TimeUnit.SECONDS)
    }

    fun disconnect() {
        networkManager?.sendPacket(
            C2SServerUnregisterPacket(serverId),
        )

        shuttingDown = true

        networkManager?.shutdown()
    }
}
