package org.readutf.orchestrator.client.heartbeat

import org.readutf.hermes.Packet
import org.readutf.orchestrator.client.capacity.ServerCapacityProducer
import org.readutf.orchestrator.shared.packets.C2SHeartbeatPacket
import org.readutf.orchestrator.shared.server.ServerHeartbeat

class HeartbeatTask(
    private val serverId: String,
    private val capacityProducer: ServerCapacityProducer,
    private val packetConsumer: (Packet) -> Unit,
) : Runnable {
    override fun run() {
        val capacity = capacityProducer.getCapacity()
        packetConsumer(
            C2SHeartbeatPacket(
                ServerHeartbeat(
                    serverId = serverId,
                    timestamp = System.currentTimeMillis(),
                    capacity = capacity,
                ),
            ),
        )
    }
}
