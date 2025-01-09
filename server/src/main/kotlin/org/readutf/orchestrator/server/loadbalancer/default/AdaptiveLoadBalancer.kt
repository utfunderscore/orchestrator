package org.readutf.orchestrator.server.loadbalancer.default

import io.github.oshai.kotlinlogging.KotlinLogging
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
    private val logger = KotlinLogging.logger { }

    // Expiry time is value
    private val pendingRequests = ArrayDeque<Long>()

    private var lastCapacity = 0.0

    override fun loadBalance(servers: Collection<RegisteredServer>): Int {
        val totalCapacity = servers.sumOf { it.getCapacity() } + virtualCapacity + getPendingRequests().size * 0.001

        logger.debug { "Total capacity: $totalCapacity" }

        val target = minServers.coerceAtLeast(maxServers.coerceAtMost(capacityToServers(totalCapacity)))

        logger.debug { "Scaling to $target" }

        lastCapacity = totalCapacity

        return target
    }

    private fun getPendingRequests(): Collection<Long> {
        synchronized(pendingRequests) {
            pendingRequests.removeIf { System.currentTimeMillis() > it }
            return pendingRequests
        }
    }

    override fun addAwaitingRequest() {
        synchronized(pendingRequests) {
            pendingRequests.add(System.currentTimeMillis() + requestDecayTime)
        }
    }

    private fun capacityToServers(totalCapacity: Double): Int {
        if (totalCapacity <= 0) {
            return 0
        }

        val totalCapacity = totalCapacity - 0.1
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
