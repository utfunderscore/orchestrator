package org.readutf.orchestrator.client.client

import com.fasterxml.jackson.databind.ObjectMapper
import org.readutf.hermes.PacketManager
import org.readutf.hermes.platform.netty.NettyClientPlatform
import org.readutf.orchestrator.client.ConnectionManager
import org.readutf.orchestrator.client.client.capacity.CapacityHandler
import org.readutf.orchestrator.client.client.shutdown.SafeShutdownHandler
import org.readutf.orchestrator.client.client.shutdown.SafeShutdownListener
import org.readutf.orchestrator.common.packets.C2SHeartbeatPacket
import org.readutf.orchestrator.common.packets.C2SUpdateAttribute
import org.readutf.orchestrator.common.server.Heartbeat
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

public class ClientManager(
    private val connectionManager: ConnectionManager,
    private val serverId: UUID,
    internal val packetManager: PacketManager<NettyClientPlatform>,
) {
    internal val taskExecutor = Executors.newSingleThreadScheduledExecutor()
    private val objectMapper = ObjectMapper()
    private var capacityHandler: CapacityHandler = CapacityHandler { 0.0 }
    internal var safeShutdownHandler: SafeShutdownHandler = SafeShutdownHandler { }
    internal var shutdownHook: () -> Unit = { }

    init {
        taskExecutor.scheduleAtFixedRate(HeartbeatTask(this), 0, 1, TimeUnit.SECONDS)

        packetManager.editListeners {
            it.registerListener(SafeShutdownListener(this, connectionManager))
        }
    }

    public fun capacityHandler(capacityHandler: CapacityHandler) {
        this.capacityHandler = capacityHandler
    }

    public fun shutdownHook(shutdownHook: () -> Unit) {
        this.shutdownHook = shutdownHook
    }

    public fun safeShutdownHandler(safeShutdownHandler: SafeShutdownHandler) {
        this.safeShutdownHandler = safeShutdownHandler
    }

    @Synchronized
    public fun updateAttribute(key: String, value: Any) {
        packetManager.sendPacket(
            C2SUpdateAttribute(
                serverId,
                key,
                objectMapper.writeValueAsBytes(value),
            ),
        )
    }

    @Synchronized
    internal fun sendHeartbeat() {
        packetManager.sendPacket(
            C2SHeartbeatPacket(
                serverId,
                Heartbeat(
                    System.currentTimeMillis(),
                    capacityHandler.getCapacity(),
                ),
            ),
        )
    }

    public fun disconnect() {
        taskExecutor.shutdown()
        shutdownHook()
    }
}
