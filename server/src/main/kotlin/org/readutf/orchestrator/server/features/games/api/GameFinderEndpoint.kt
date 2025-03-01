package org.readutf.orchestrator.server.features.games.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.github.michaelbull.result.runCatching
import io.github.oshai.kotlinlogging.KotlinLogging
import io.javalin.websocket.WsConnectContext
import io.javalin.websocket.WsMessageContext
import org.readutf.orchestrator.common.api.ApiResponse
import org.readutf.orchestrator.server.features.games.GameManager
import org.readutf.orchestrator.server.utils.WebSocketEndpoint
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

class GameFinderEndpoint(private val gameManager: GameManager) : WebSocketEndpoint {

    private val logger = KotlinLogging.logger { }
    private val objectMapper = jacksonObjectMapper()

    private var gameType: String by Delegates.notNull()
    private var timeout: Long = 5000

    override fun handleConnect(ctx: WsConnectContext) {
        gameType = ctx.pathParam("type")
        timeout = ctx.queryParam("timeout")?.toLongOrNull() ?: 10_000
    }

    override fun handleMessage(ctx: WsMessageContext) {
        val players = runCatching {
            ctx.messageAsClass<List<UUID>>()
        }.getOrElse { e ->
            ctx.send(ApiResponse.error("Failed to read json, must be list of UUIDs."))
            return
        }

        gameManager.findGameServer(
            gameType = gameType,
            players = players,
        ).orTimeout(timeout, TimeUnit.MILLISECONDS)
            .thenAcceptAsync {
                it.onSuccess { gameServer ->
                    ctx.send(ApiResponse.success(gameServer))
                }.onFailure { e ->
                    logger.error(e) { "Failed to find game server." }
                    ctx.send(ApiResponse.error("Failed to find game server."))
                }
            }.exceptionallyAsync {
                logger.error(it) { "Failed to find game server." }
                ctx.send(ApiResponse.error("Failed to find game server."))
                null
            }
    }
}
