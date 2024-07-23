package org.readutf.orchestrator.client.game

import org.readutf.orchestrator.shared.game.GameRequest
import org.readutf.orchestrator.shared.game.GameRequestResult
import org.readutf.orchestrator.shared.utils.Result

interface GameRequestHandler {
    fun handleRequest(request: GameRequest): Result<GameRequestResult>
}
