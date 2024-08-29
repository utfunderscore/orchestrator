package org.readutf.orchestrator.client

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.orchestrator.client.network.NetworkManager
import org.readutf.orchestrator.client.server.ServerManager
import org.readutf.orchestrator.shared.game.GameFinderType
import org.readutf.orchestrator.shared.server.ServerAddress
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class OrchestratorClient(
    val orchestratorHost: String,
    val orchestratorPort: Int,
    val serverAddress: ServerAddress,
    val gameTypes: List<String> = emptyList(),
    val gameFinders: List<GameFinderType> = emptyList(),
    private val autoReconnect: Boolean = true,
    private val maxReconnectAttempts: Int = 15,
    private val onConnect: (ServerManager) -> Unit = {},
) {
    private val logger = KotlinLogging.logger { }

    private val serverId = UUID.randomUUID()
    private var networkManager: NetworkManager? = null
    private var serverManager: ServerManager? = null

    private val reconnectScheduler = Executors.newSingleThreadScheduledExecutor()
    private var shuttingDown = false
    private var reconnectAttempts = 0

    fun connect() {
        logger.info { "Connecting to orchestrator... ($orchestratorHost:$orchestratorPort)" }
        try {
            networkManager = NetworkManager(this)
            serverManager = ServerManager(serverId, serverAddress, gameTypes, gameFinders, networkManager!!)
            onConnect(serverManager!!)
            reconnectAttempts = 0
        } catch (e: Exception) {
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

    private fun disconnect() {
        shuttingDown = true

        networkManager?.shutdown()
    }
}

fun main() {
    OrchestratorClient(
        orchestratorHost = "localhost",
        orchestratorPort = 2980,
        serverAddress = ServerAddress("localhost", 9394),
        onConnect = {
            it.registerServer()
            it.scheduleHeartbeat()
        },
    ).connect()
}
