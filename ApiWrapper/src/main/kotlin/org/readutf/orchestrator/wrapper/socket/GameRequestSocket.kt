package org.readutf.orchestrator.wrapper.socket

import com.fasterxml.jackson.core.type.TypeReference
import io.github.oshai.kotlinlogging.KotlinLogging
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.readutf.orchestrator.shared.game.GameRequest
import org.readutf.orchestrator.shared.game.GameRequestResult
import org.readutf.orchestrator.wrapper.OrchestratorApi
import java.lang.Exception
import java.net.URI
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

internal class GameRequestSocket(
    uri: String,
) : WebSocketClient(URI(uri)) {
    private val logger = KotlinLogging.logger { }

    private val futures = mutableMapOf<UUID, CompletableFuture<GameRequestResult>>()

    fun requestGame(
        gameType: String,
        timeout: Long = 5000,
    ): CompletableFuture<GameRequestResult> {
        val requestId = UUID.randomUUID()
        val gameRequest = GameRequest(requestId, gameType)

        if (!isOpen) {
            logger.info { "Connecting to game request socket..." }
            connectBlocking()
            logger.info { "Connected to game request socket" }
        }

        send(OrchestratorApi.objectMapper.writeValueAsString(gameRequest))

        val future = CompletableFuture<GameRequestResult>()

        futures[requestId] = future

        return future.orTimeout(timeout, TimeUnit.MILLISECONDS)
    }

    override fun onOpen(p0: ServerHandshake?) {
        logger.debug { "Connected to game request websocket" }
    }

    override fun onMessage(message: String?) {
        println(message)
        OrchestratorApi.objectMapper.readValue(message, object : TypeReference<GameRequestResult>() {})?.let { gameRequestResult ->
            futures[gameRequestResult.requestId]?.complete(gameRequestResult)
        }
    }

    override fun onClose(
        p0: Int,
        p1: String?,
        p2: Boolean,
    ) {
    }

    override fun onError(p0: Exception?) {
    }
}
