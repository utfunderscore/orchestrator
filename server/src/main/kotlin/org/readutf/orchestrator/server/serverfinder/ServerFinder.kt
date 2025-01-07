package org.readutf.orchestrator.server.serverfinder

import com.fasterxml.jackson.databind.JsonNode
import org.readutf.orchestrator.common.server.Server
import org.readutf.orchestrator.common.utils.SResult
import java.util.concurrent.CompletableFuture

interface ServerFinder {
    fun findServer(args: JsonNode): CompletableFuture<SResult<Server>>
}
