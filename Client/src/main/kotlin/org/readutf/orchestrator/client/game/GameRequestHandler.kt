package org.readutf.orchestrator.client.game

import org.readutf.orchestrator.shared.game.GameRequest
import org.readutf.orchestrator.shared.game.GameRequestResult

interface GameRequestHandler {
    fun handleRequest(request: GameRequest): GameRequestResult
}
