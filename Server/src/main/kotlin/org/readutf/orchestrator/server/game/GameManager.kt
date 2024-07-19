package org.readutf.orchestrator.server.game

import io.javalin.Javalin
import org.readutf.orchestrator.server.game.endpoints.GameRequestSocket

class GameManager(
    javalin: Javalin,
) {
    init {
        javalin.ws("/game/request", GameRequestSocket(this))
    }
}
