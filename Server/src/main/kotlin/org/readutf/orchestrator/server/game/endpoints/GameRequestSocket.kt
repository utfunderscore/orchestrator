package org.readutf.orchestrator.server.game.endpoints

import com.alibaba.fastjson.JSON
import io.github.oshai.kotlinlogging.KotlinLogging
import io.javalin.websocket.WsConfig
import org.readutf.orchestrator.server.game.GameManager
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.shared.game.GameRequest
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

            context.send(gameManager.findMatch(gameRequest))
        }

        ctx.onConnect {
            it.enableAutomaticPings()
        }
    }
}
