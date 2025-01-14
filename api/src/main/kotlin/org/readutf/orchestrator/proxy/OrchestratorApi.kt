package org.readutf.orchestrator.proxy

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.readutf.orchestrator.common.server.Server
import org.readutf.orchestrator.common.utils.SResult
import org.readutf.orchestrator.proxy.api.ServerFinderService
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class OrchestratorApi(
    private val hostname: String,
    private val port: Int = 9191,
) {
    fun findServerBlocking(
        serverType: String,
        connectionTimeout: Long = 2_000,
        findServerTimeout: Long = 10_000,
    ): CompletableFuture<SResult<Server>> {
        val future = CompletableFuture<SResult<Server>>()
        ServerFinderService(
            "ws://$hostname:$port/serverfinder/$serverType",
            future,
        ).connectBlocking(connectionTimeout, TimeUnit.MILLISECONDS)
        return future.orTimeout(findServerTimeout, TimeUnit.MILLISECONDS)
    }

    companion object {
        val objectMapper = jacksonObjectMapper { }
    }
}
