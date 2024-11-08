package org.readutf.orchestrator.client.listeners

import org.readutf.hermes.channel.HermesChannel
import org.readutf.hermes.listeners.TypedListener
import org.readutf.orchestrator.client.capacity.ServerCapacityProducer
import org.readutf.orchestrator.shared.packets.S2CRecalculateCapacity

class CapacityRequestListener(
    private val capacityProducer: ServerCapacityProducer,
) : TypedListener<S2CRecalculateCapacity, HermesChannel, Float> {
    override fun handle(
        packet: S2CRecalculateCapacity,
        channel: HermesChannel,
    ): Float = capacityProducer.getCapacity()
}
