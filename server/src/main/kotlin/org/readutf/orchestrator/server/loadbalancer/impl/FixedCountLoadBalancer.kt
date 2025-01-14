package org.readutf.orchestrator.server.loadbalancer.impl

import org.readutf.orchestrator.server.loadbalancer.LoadBalancer
import org.readutf.orchestrator.server.server.RegisteredServer

class FixedCountLoadBalancer(
    private val count: Int,
) : LoadBalancer {
    override fun loadBalance(servers: Collection<RegisteredServer>): Int = count

    override fun addAwaitingRequest() {
        // No-op
    }
}
