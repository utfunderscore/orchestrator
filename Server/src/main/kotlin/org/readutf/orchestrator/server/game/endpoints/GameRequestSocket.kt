package org.readutf.orchestrator.server.game.endpoints

import com.alibaba.fastjson.JSON
import io.github.oshai.kotlinlogging.KotlinLogging
import io.javalin.websocket.WsConfig
import org.readutf.orchestrator.server.game.GameManager
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.shared.game.GameRequest
import org.readutf.orchestrator.shared.utils.ApiResponse
import java.util.function.Consumer

class GameRequestSocket(
    private val gameManager: GameManager,
    private val serverManager: ServerManager,
) : Consumer<WsConfig> {
    private val logger = KotlinLogging.logger { }

    override fun accept(ctx: WsConfig) {
        ctx.onMessage { context ->
            val messageJson = context.message()

            val gameRequest = JSON.parseObject(messageJson, GameRequest::class.java)
            if (gameRequest == null) {
                context.send("Invalid Request")
                context.closeSession()
                return@onMessage
            }

            logger.info { "Received Game Request: $gameRequest" }

            gameManager.findMatch(gameRequest).thenAccept {
                if (it.isError()) {
                    context.send(JSON.toJSONString(ApiResponse.failure<String>(it.getError())))
                } else {
                    context.send(JSON.toJSONString(ApiResponse.success(it.get())))
                }
            }
        }

        ctx.onConnect {
            it.enableAutomaticPings()
        }
    }
}
