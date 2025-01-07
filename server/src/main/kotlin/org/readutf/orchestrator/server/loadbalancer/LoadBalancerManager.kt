package org.readutf.orchestrator.server.loadbalancer

import org.readutf.orchestrator.server.container.scale.ScaleManager
import org.readutf.orchestrator.server.loadbalancer.default.AdaptiveLoadBalancer
import org.readutf.orchestrator.server.server.ServerManager
import java.util.concurrent.Executors

class LoadBalancerManager(
    private val serverManager: ServerManager,
    private val scaleManager: ScaleManager,
) {
    private val executor = Executors.newSingleThreadExecutor()
    private val loadBalancers = mutableMapOf<String, LoadBalancer>()

    init {
        executor.submit(this::tick)
    }

    fun getLoadBalancer(serverType: String) = loadBalancers.getOrPut(serverType) { generateDefault(serverType) }

    fun generateDefault(serverType: String) =
        AdaptiveLoadBalancer(
            serverType = serverType,
            increaseThreshold = 0.75,
            decreaseThreshold = 0.25,
            virtualCapacity = 0.0,
            minServers = 0,
            maxServers = Integer.MAX_VALUE,
            requestDecayTime = 5000,
        )

    fun tick() {
        val servers = serverManager.getServers()
        for ((type, loadBalancer) in loadBalancers) {
            val change = loadBalancer.loadBalance(servers)
            val newTarget = scaleManager.getScale(type) + change

            scaleManager.scaleDeployment(type, newTarget)
        }
    }
}
