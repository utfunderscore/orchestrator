package org.readutf.orchestrator.client.capacity

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.orchestrator.client.client.capacity.CapacityHandler

class DefaultCapacityHandler(
    val capacitySupplier: () -> Double,
) : CapacityHandler {
    private val logger = KotlinLogging.logger { }

    override fun getCapacity(): Double {
        val capacity = capacitySupplier()

        if (capacity < 0.0 || capacity > 1.0) {
            logger.error { "Capacity must be between 0.0 and 1.0" }
        }

        return 0.0
    }
}
