package org.readutf.orchestrator.server.serverfinder

import com.fasterxml.jackson.databind.JsonNode
import com.github.michaelbull.result.Result
import org.readutf.orchestrator.common.server.Server
import java.util.concurrent.CompletableFuture

interface ServerFinder {
    fun findServer(args: JsonNode): CompletableFuture<Result<Server, Throwable>>
}
