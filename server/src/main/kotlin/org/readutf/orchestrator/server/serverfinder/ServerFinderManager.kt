package org.readutf.orchestrator.server.serverfinder

import com.fasterxml.jackson.databind.JsonNode
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import org.readutf.orchestrator.common.server.Server
import org.readutf.orchestrator.common.template.TemplateName
import org.readutf.orchestrator.server.loadbalancer.LoadBalancerManager
import org.readutf.orchestrator.server.server.ServerManager
import java.util.concurrent.CompletableFuture

class ServerFinderManager(
    private val loadBalancerManager: LoadBalancerManager,
    private val serverManager: ServerManager,
) {
    private val serverFinders = mutableMapOf<TemplateName, ServerFinder>()

    fun findServer(
        templateName: TemplateName,
        jsonNode: JsonNode,
    ): CompletableFuture<Result<Server, Throwable>> {
        val finder = serverFinders.getOrElse(templateName) {
            return CompletableFuture.completedFuture(Err(IllegalStateException("No server finder for $templateName")))
        }
        return finder.findServer(jsonNode)
    }
}
