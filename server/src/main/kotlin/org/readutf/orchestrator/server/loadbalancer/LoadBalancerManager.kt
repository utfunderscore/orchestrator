package org.readutf.orchestrator.server.loadbalancer

import com.github.michaelbull.result.onFailure
import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.orchestrator.server.container.scale.ScaleManager
import org.readutf.orchestrator.server.loadbalancer.impl.AdaptiveLoadBalancer
import org.readutf.orchestrator.server.loadbalancer.impl.FixedCountLoadBalancer
import org.readutf.orchestrator.server.server.ServerManager
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class LoadBalancerManager(
    private val serverManager: ServerManager,
    private val scaleManager: ScaleManager,
) {
    private val logger = KotlinLogging.logger { }
    private val executor = Executors.newSingleThreadScheduledExecutor()
    private val loadBalancers = mutableMapOf<String, LoadBalancer>()

    init {
        logger.info { "Starting load balancer..." }
        executor.scheduleAtFixedRate(this::tick, 0, 1, TimeUnit.SECONDS)

        // TODO: Load from config, or use a service to register load balancers
        loadBalancers["edge-node"] = FixedCountLoadBalancer(1)
        loadBalancers["minigame"] = FixedCountLoadBalancer(1)
    }

    fun getLoadBalancer(serverType: String) = loadBalancers.getOrPut(serverType) { generateDefault(serverType) }

    private fun generateDefault(serverType: String) = AdaptiveLoadBalancer(
        serverType = serverType,
        increaseThreshold = 0.75,
        decreaseThreshold = 0.25,
        virtualCapacity = 0.0,
        minServers = 0,
        maxServers = Integer.MAX_VALUE,
        requestDecayTime = 15_000,
    )

    fun tick() {
        val servers = serverManager.getServers()

        for ((type, loadBalancer) in loadBalancers) {
            val target = loadBalancer.loadBalance(servers)

            scaleManager.scaleDeployment(type, target).onFailure {
                println("Failed to scale deployment $type: $it")
            }
        }
    }
}
