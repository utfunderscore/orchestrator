package org.readutf.orchestrator.client.capacity.defaults

import org.readutf.orchestrator.client.capacity.ServerCapacityProducer
import java.util.concurrent.atomic.AtomicReference

class ManualCapacityProducer : ServerCapacityProducer {
    @Volatile private var capacity = AtomicReference(0f)

    override fun getCapacity(): Float = capacity.get()

    fun setCapacity(capacity: Float) {
        require(capacity in 0f..1f) { "Capacity must be between 0 and 1" }
        this.capacity.set(capacity)
    }
}
