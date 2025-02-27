package org.readutf.orchestrator.client.client.game

import com.github.michaelbull.result.Result
import java.util.UUID

public fun interface GameRequestHandler {

    public fun startGame(gameType: String, players: List<UUID>): Result<UUID, Throwable>
}
