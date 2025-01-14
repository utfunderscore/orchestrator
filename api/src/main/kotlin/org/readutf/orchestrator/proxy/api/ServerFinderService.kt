package org.readutf.orchestrator.proxy.api

import com.fasterxml.jackson.core.type.TypeReference
import com.github.michaelbull.result.*
import io.github.oshai.kotlinlogging.KotlinLogging
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.readutf.orchestrator.common.api.ApiResponse
import org.readutf.orchestrator.common.server.Server
import org.readutf.orchestrator.common.utils.SResult
import org.readutf.orchestrator.proxy.OrchestratorApi
import java.net.URI
import java.util.concurrent.CompletableFuture

class ServerFinderService(
    uri: String,
    private val serverFuture: CompletableFuture<SResult<Server>>,
) : WebSocketClient(URI(uri)) {
    private val logger = KotlinLogging.logger {}

    override fun onOpen(handshakedata: ServerHandshake) {
    }

    override fun onMessage(message: String) {
        logger.info { "Received message: $message" }

        val result: Result<Server, String> =
            runCatching {
                // Safely convert the message to a Server object
                OrchestratorApi.objectMapper.readValue(
                    message,
                    object : TypeReference<ApiResponse<Server>>() {},
                )
            }.mapError {
                // Map any exceptions to a failure message
                it.message ?: "Unknown error"
            }.flatMap {
                // Convert the ApiResponse to a Result
                apiResponseToResult(it)
            }

        serverFuture.complete(result)
    }

    private fun apiResponseToResult(apiResponse: ApiResponse<Server>): SResult<Server> {
        val result = apiResponse.result
        return if (result != null) {
            Ok(result)
        } else {
            Err(apiResponse.failureMessage ?: "Unknown error")
        }
    }

    override fun onClose(
        code: Int,
        reason: String,
        remote: Boolean,
    ) {
        if (serverFuture.isDone) return
        serverFuture.complete(Err("Connection closed with no response"))
    }

    override fun onError(ex: Exception?) {
        logger.error(ex) { "Exception occurred finding a server" }

        close()

        if (serverFuture.isDone) return
        logger.error(ex) { "Connection closed with no response" }
        serverFuture.complete(Err("Connection closed with no response"))
    }
}
