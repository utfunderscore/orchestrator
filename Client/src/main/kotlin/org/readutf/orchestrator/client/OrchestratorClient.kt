package org.readutf.orchestrator.client

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.annotations.Blocking
import org.readutf.orchestrator.client.capacity.ServerCapacityProducer
import org.readutf.orchestrator.client.capacity.defaults.ManualCapacityProducer
import org.readutf.orchestrator.client.settings.ConnectionSettings
import org.readutf.orchestrator.shared.server.ServerAddress
import java.util.UUID
import java.util.concurrent.CompletableFuture

class OrchestratorClient(
    val serverId: String,
    val serverType: String,
    val localAddress: ServerAddress,
    val connectionSettings: ConnectionSettings,
    val capacityProducer: ServerCapacityProducer,
    val onConnect: ClientManager.() -> Unit,
    val onDisconnect: ClientManager.() -> Unit,
) {
    private val lock = Any()
    private val logger = KotlinLogging.logger {}
    private var clientManager: ClientManager? = null

    /**
     * Connect to the orchestrator server
     * @return [CompletableFuture] that completes when the connection has exceeded the reconnect attempts
     */
    fun connect(onExit: Runnable) {
        Thread {
            connectBlocking(onExit)
        }.start()
    }

    @Blocking
    fun connectBlocking(onExit: Runnable) {
        var reconnectAttempts = 0

        while (connectionSettings.maxReconnectAttempts == -1 || reconnectAttempts <= connectionSettings.maxReconnectAttempts) {
            if (reconnectAttempts > 0) {
                logger.info { "Connection failed, reconnecting in 5 seconds..." }
                Thread.sleep(5000)
            }

            synchronized(lock) {
                val successful =
                    ClientManager(
                        remoteAddress = connectionSettings.remoteAddress,
                        remotePort = connectionSettings.remotePort,
                        serverId = serverId,
                        serverType = serverType,
                        localAddress = localAddress,
                        capacityProducer = capacityProducer,
                        onConnect = onConnect,
                        onDisconnect = onDisconnect,
                    ).start().join()

                if (!successful) reconnectAttempts++

                if (!connectionSettings.autoReconnect) {
                    return
                }
            }
        }

        onExit.run()
    }

    fun disconnect() {
        synchronized(lock) {
            clientManager?.disconnect()
        }
    }
}

fun main() {
    val logger = KotlinLogging.logger {}

    OrchestratorClient(
        serverId = UUID.randomUUID().toString(),
        serverType = "lobby",
        localAddress = ServerAddress("localhost", 25565),
        connectionSettings =
            ConnectionSettings(
                remoteAddress = "localhost",
                remotePort = 2980,
                autoReconnect = true,
                maxReconnectAttempts = 5,
            ),
        capacityProducer = ManualCapacityProducer(),
        onConnect = {
            logger.info { "Connected to orchestrator" }
        },
        onDisconnect = {
            logger.info { "Disconnected from orchestrator" }
        },
    ).connect {
        println("Exiting...")
    }
}
