package org.readutf.orchestrator.client.capacity

fun interface ServerCapacityProducer {
    fun getCapacity(): Float
}
