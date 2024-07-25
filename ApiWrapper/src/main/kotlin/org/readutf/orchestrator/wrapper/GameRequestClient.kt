package org.readutf.orchestrator.wrapper

import com.alibaba.fastjson2.JSON
import io.github.oshai.kotlinlogging.KotlinLogging
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.readutf.orchestrator.shared.game.GameRequest
import org.readutf.orchestrator.shared.game.GameRequestResult
import java.lang.Exception
import java.net.URI
import java.util.UUID
import java.util.concurrent.CompletableFuture

class GameRequestClient(
    uri: String,
) : WebSocketClient(URI(uri)) {
    private val logger = KotlinLogging.logger { }
    private val futures = mutableMapOf<UUID, CompletableFuture<GameRequestResult>>()

    fun requestGame(
        gameType: String,
        numOfTeams: Int,
        teamSize: Int,
    ): CompletableFuture<GameRequestResult> {
        val requestId = UUID.randomUUID()
        val gameRequest = GameRequest(requestId, gameType, numOfTeams, teamSize)

        if (!isOpen) connect()

        send(JSON.toJSONString(gameRequest))

        val future = CompletableFuture<GameRequestResult>()

        futures[requestId] = future

        return future
    }

    override fun onOpen(p0: ServerHandshake?) {
        logger.info { "Connected to websocket" }
    }

    override fun onMessage(p0: String?) {
        println(p0)
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
