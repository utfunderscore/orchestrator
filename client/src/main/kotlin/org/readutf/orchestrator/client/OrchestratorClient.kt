package org.readutf.orchestrator.client

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.util.Pool
import org.readutf.orchestrator.client.client.capacity.CapacityHandler
import org.readutf.orchestrator.client.client.shutdown.SafeShutdownHandler
import org.readutf.orchestrator.client.platform.ContainerPlatform
import org.readutf.orchestrator.common.packets.KryoBuilder

class OrchestratorClient(
    private val hostAddress: String,
    private val maxReconnectAttempts: Int,
    private val platform: ContainerPlatform,
    private val reconnectDelay: Long = 5000,
    private val capacityHandler: CapacityHandler,
) {
    private var connectionManager: ConnectionManager? = null
    private var reconnectAttempts = 0

    var shutdownHook: () -> Unit = {}
    var connectHandle: (ConnectionManager) -> Unit = {}

    fun connectBlocking() {
        while (reconnectAttempts++ < maxReconnectAttempts) {
            val connectionManager =
                ConnectionManager(
                    kryoPool =
                        object : Pool<Kryo>(true, false) {
                            override fun create(): Kryo = KryoBuilder.build()
                        },
                    hostAddress = hostAddress,
                    port = 2323,
                    containerId = platform.getContainerId(),
                    capacityHandler = capacityHandler,
                )

            this.connectionManager = connectionManager

            val result = connectionManager.connectBlocking(connectHandle)
            if (result) {
                reconnectAttempts = 0
            } else {
                Thread.sleep(reconnectDelay)
            }
        }

        shutdownHook()
    }

    fun addSafeShutdownHandler(handler: SafeShutdownHandler) {
        val connectionManager = connectionManager ?: throw IllegalStateException("Connection manager is not initialized")
        connectionManager.registerSafeShutdownListener(handler)
    }

    fun onConnect(handle: (ConnectionManager) -> Unit) {
        connectHandle = handle
    }
}
