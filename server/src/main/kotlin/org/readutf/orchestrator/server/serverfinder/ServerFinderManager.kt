package org.readutf.orchestrator.server.serverfinder

import com.fasterxml.jackson.databind.JsonNode
import org.readutf.orchestrator.common.server.Server
import org.readutf.orchestrator.common.utils.SResult
import org.readutf.orchestrator.server.loadbalancer.LoadBalancerManager
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.server.serverfinder.impl.DefaultServerFinder
import java.util.concurrent.CompletableFuture

class ServerFinderManager(
    private val loadBalancerManager: LoadBalancerManager,
    private val serverManager: ServerManager,
) {
    private val serverFinders = mutableMapOf<String, ServerFinder>()

    fun findServer(
        serverType: String,
        jsonNode: JsonNode,
    ): CompletableFuture<SResult<Server>> {
        val finder = serverFinders.getOrPut(serverType) { createDefaultFinder(serverType) }
        return finder.findServer(jsonNode)
    }

    private fun createDefaultFinder(serverType: String): DefaultServerFinder =
        DefaultServerFinder(
            serverType = serverType,
            loadBalancerManager = loadBalancerManager,
            serverManager = serverManager,
        )
}
