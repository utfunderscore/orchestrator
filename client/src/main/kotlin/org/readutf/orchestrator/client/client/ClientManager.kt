package org.readutf.orchestrator.client.client

import org.readutf.hermes.PacketManager
import org.readutf.hermes.platform.netty.NettyClientPlatform
import org.readutf.orchestrator.client.client.capacity.CapacityHandler
import org.readutf.orchestrator.common.packets.C2SHeartbeatPacket
import org.readutf.orchestrator.common.server.Heartbeat
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ClientManager(
    private val serverId: UUID,
    private val packetManager: PacketManager<NettyClientPlatform>,
    private val capacityHandler: CapacityHandler,
) {
    private val heartbeatExecutor = Executors.newSingleThreadScheduledExecutor()

    init {
        heartbeatExecutor.scheduleAtFixedRate(HeartbeatTask(this), 0, 1, TimeUnit.SECONDS)
    }

    @Synchronized
    fun sendHeartbeat() {
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

    fun disconnect() {
        heartbeatExecutor.shutdown()
    }
}
