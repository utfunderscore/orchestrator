package org.readutf.orchestrator.server.serverfinder.impl

import com.fasterxml.jackson.databind.JsonNode
import org.readutf.orchestrator.common.server.Server
import org.readutf.orchestrator.common.utils.SResult
import org.readutf.orchestrator.server.loadbalancer.LoadBalancerManager
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.server.serverfinder.ServerFinder
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

class DefaultServerFinder(
    val serverType: String,
    val loadBalancerManager: LoadBalancerManager,
    val serverManager: ServerManager,
    val minFillThreshold: Int = 5,
) : ServerFinder {
    private val executor = Executors.newCachedThreadPool()

    override fun findServer(args: JsonNode): CompletableFuture<SResult<Server>> {
        val loadBalancer = loadBalancerManager.getLoadBalancer(serverType)

        val servers =
            serverManager
                .getServers()
                .map { server ->
                    Pair<Server, Double>(server, Math.max(minFillThreshold.toDouble(), server.getCapacity()))
                }.sortedBy { it.second }

        CompletableFuture.supplyAsync({
            return@supplyAsync 0
        }, executor)

        TODO()
    }
}
