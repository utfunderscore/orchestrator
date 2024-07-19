package org.readutf.orchestrator.server.game.endpoints

import com.alibaba.fastjson2.JSON
import io.javalin.websocket.WsConfig
import org.readutf.orchestrator.server.game.GameManager
import org.readutf.orchestrator.shared.game.GameRequest
import java.util.function.Consumer

class GameRequestSocket(
    val gameManager: GameManager,
) : Consumer<WsConfig> {
    override fun accept(ctx: WsConfig) {
        ctx.onMessage { context ->
            val messageJson = context.message()

            val typedJson = JSON.parseObject(messageJson, GameRequest::class.java)
            if (typedJson == null) {
                context.send("Invalid Request")
                context.closeSession()
                return@onMessage
            }

//            gameManager.handleRequest(context, typedJson)
        }
    }
}
