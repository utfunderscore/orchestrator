package org.readutf.orchestrator.client

import com.esotericsoftware.kryo.kryo5.Kryo
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
    val kryo: Kryo,
    private val onConnect: (ServerManager) -> Unit,
    val gameTypes: List<String> = emptyList(),
    val gameFinders: List<GameFinderType> = emptyList(),
    private val autoReconnect: Boolean = true,
    private val maxReconnectAttempts: Int = 15,
) {
    private val logger = KotlinLogging.logger { }

    private val serverId = UUID.randomUUID()
    var networkManager: NetworkManager? = null
    private var serverManager: ServerManager? = null

    private val reconnectScheduler = Executors.newSingleThreadScheduledExecutor()
    private var shuttingDown = false
    private var reconnectAttempts = 0

    fun connect() {
        logger.info { "Connecting to orchestrator... ($orchestratorHost:$orchestratorPort)" }
        try {
            networkManager =
                NetworkManager(orchestratorHost, orchestratorPort, kryo, ::onDisconnect)
            serverManager = ServerManager(serverId, serverAddress, gameTypes, gameFinders, networkManager!!)
            val executor = Executors.newSingleThreadScheduledExecutor()
            executor.schedule({
                if (networkManager != null) {
                    onConnect(serverManager!!)
                }
            }, 10, TimeUnit.MILLISECONDS)
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
