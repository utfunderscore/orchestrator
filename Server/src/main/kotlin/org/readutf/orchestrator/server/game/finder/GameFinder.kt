package org.readutf.orchestrator.server.game.finder

import org.readutf.orchestrator.shared.game.GameFinderType
import org.readutf.orchestrator.shared.game.GameRequest
import org.readutf.orchestrator.shared.game.GameRequestResult
import org.readutf.orchestrator.shared.utils.Result

abstract class GameFinder(
    val gameFinderType: GameFinderType,
) {
    abstract fun findGame(gameRequest: GameRequest): Result<GameRequestResult>
}
