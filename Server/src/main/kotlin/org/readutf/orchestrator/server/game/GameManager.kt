package org.readutf.orchestrator.server.game

import io.javalin.Javalin
import io.javalin.websocket.WsContext
import org.readutf.orchestrator.server.game.endpoints.GameRequestSocket

class GameManager(
    javalin: Javalin,
) {
    init {
        javalin.ws("/game/request", GameRequestSocket(this))
    }

    fun handleRequest(
        wsContext: WsContext,
        gameRequest: GameRequest,
    ) {
    }
}
