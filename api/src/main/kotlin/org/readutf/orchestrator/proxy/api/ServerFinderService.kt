package org.readutf.orchestrator.proxy.api

import com.fasterxml.jackson.core.type.TypeReference
import com.github.michaelbull.result.*
import io.github.oshai.kotlinlogging.KotlinLogging
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.readutf.orchestrator.common.server.Server
import org.readutf.orchestrator.proxy.OrchestratorApi
import java.net.URI
import java.util.concurrent.CompletableFuture

class ServerFinderService(
    uri: String,
    private val serverFuture: CompletableFuture<Result<Server, Throwable>>,
) : WebSocketClient(URI(uri)) {
    private val logger = KotlinLogging.logger {}

    override fun onOpen(handshakedata: ServerHandshake) {
    }

    override fun onMessage(message: String) {
        logger.info { "Received message: $message" }

        val result: Result<Server, Throwable> =
            runCatching {
                OrchestratorApi.objectMapper.readValue(
                    message,
                    object : TypeReference<Server>() {},
                )
            }

        serverFuture.complete(result)
    }

    override fun onClose(
        code: Int,
        reason: String,
        remote: Boolean,
    ) {
        if (serverFuture.isDone) return
        serverFuture.complete(Err(Exception("Connection closed with no response")))
    }

    override fun onError(ex: Exception?) {
        logger.error(ex) { "Exception occurred finding a server" }

        close()

        if (serverFuture.isDone) return
        logger.error(ex) { "Connection closed with no response" }
        serverFuture.complete(Err(Exception("Connection closed with no response")))
    }
}
