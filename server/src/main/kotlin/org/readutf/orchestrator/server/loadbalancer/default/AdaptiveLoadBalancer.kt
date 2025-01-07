package org.readutf.orchestrator.server.loadbalancer.default

import org.readutf.orchestrator.server.loadbalancer.LoadBalancer
import org.readutf.orchestrator.server.server.RegisteredServer

class AdaptiveLoadBalancer(
    private val serverType: String,
    private val increaseThreshold: Double,
    private val decreaseThreshold: Double,
    private val virtualCapacity: Double,
    private val minServers: Int,
    private val maxServers: Int,
    private val requestDecayTime: Long,
) : LoadBalancer {
    // Expiry time is value
    private val pendingRequests = ArrayDeque<Long>()

    override fun loadBalance(servers: Collection<RegisteredServer>): Int {
        val totalCapacity = servers.sumOf { it.getCapacity() } + virtualCapacity + getPendingRequests().size * 0.2

        return minServers.coerceAtLeast(maxServers.coerceAtMost(capacityToServers(totalCapacity)))
    }

    fun getPendingRequests(): Collection<Long> {
        pendingRequests.removeIf { System.currentTimeMillis() > it }
        return pendingRequests
    }

    override fun addAwaitingRequest() {
        pendingRequests.add(System.currentTimeMillis() + requestDecayTime)
    }

    private fun capacityToServers(totalCapacity: Double): Int {
        val num = totalCapacity.toInt()
        val frac = totalCapacity - num

        return if (frac > increaseThreshold) {
            num + 3 // Increase server count
        } else if (frac < decreaseThreshold) {
            num + 1 // Decrease the server count
        } else {
            num + 2 // Keep server count the same
        }
    }
}
