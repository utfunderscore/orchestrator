package org.readutf.orchestrator.server.loadbalancer.impl

import org.readutf.orchestrator.server.loadbalancer.Autoscaler
import org.readutf.orchestrator.server.server.RegisteredServer

class FixedCountAutoscaler(
    val count: Int,
) : Autoscaler("fixed_count") {
    override fun getNeededResources(servers: Collection<RegisteredServer>): Int = count

    override fun addAwaitingRequest() {
        // No-op
    }

    override fun toString(): String = "FixedCountAutoscaler(count=$count)"
}
