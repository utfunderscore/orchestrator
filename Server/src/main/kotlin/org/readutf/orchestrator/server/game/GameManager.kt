package org.readutf.orchestrator.server.game

import io.javalin.Javalin
import io.javalin.websocket.WsContext
import org.readutf.orchestrator.server.game.endpoints.GameRequestSocket
import org.readutf.orchestrator.shared.game.GameRequest

class GameManager(
    javalin: Javalin,
) {
    init {
        javalin.ws("/game/request", GameRequestSocket(this))
    }


}
