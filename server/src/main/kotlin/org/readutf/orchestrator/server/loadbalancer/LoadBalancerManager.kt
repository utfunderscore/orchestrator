package org.readutf.orchestrator.server.loadbalancer

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.orchestrator.common.template.TemplateName
import org.readutf.orchestrator.server.server.ServerManager
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class LoadBalancerManager(
    private val serverManager: ServerManager,
) {
    private val logger = KotlinLogging.logger { }
    private val executor = Executors.newSingleThreadScheduledExecutor()
    private val loadBalancers = mutableMapOf<TemplateName, LoadBalancer>()

    init {
        logger.info { "Starting load balancer..." }
        executor.scheduleAtFixedRate(this::tick, 0, 1, TimeUnit.SECONDS)

        // TODO: Load from config, or use a service to register load balancers
//        loadBalancers["edge-node"] = FixedCountLoadBalancer(1)
//        loadBalancers["minigame"] = FixedCountLoadBalancer(1)
    }

    fun getLoadBalancer(templateName: TemplateName) = loadBalancers[templateName]

    fun tick() {
        val servers = serverManager.getServers()

        for ((type, loadBalancer) in loadBalancers) {
            val target = loadBalancer.loadBalance(servers)

//            scaleManager.scaleDeployment(type, target).onFailure {
//                println("Failed to scale deployment $type: $it")
//            }
        }
    }
}
