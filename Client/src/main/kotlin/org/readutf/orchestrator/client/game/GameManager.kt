package org.readutf.orchestrator.client.game

import org.readutf.orchestrator.client.network.ClientNetworkManager
import org.readutf.orchestrator.shared.game.Game
import org.readutf.orchestrator.shared.game.GameState
import java.util.UUID
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class GameManager(
    private val networkManager: ClientNetworkManager,
    private val gameRequestHandler: GameRequestHandler,
    scheduler: ScheduledExecutorService,
) {
    private val games = mutableMapOf<UUID, Game>()

    init {
        scheduler.scheduleAtFixedRate(
            { networkManager.updateGames(games.values.toList()) },
            0,
            5,
            TimeUnit.SECONDS,
        )
    }

    fun registerGame(game: Game) {
        games[game.id] = game
    }

    fun unregisterGame(game: Game) {
        games.remove(game.id)
    }

    fun reserveGame(gameId: UUID): Boolean {
        games[gameId]?.let {
            it.gameState = GameState.AWAITING_PLAYERS
            return true
        }
        return false
    }
}
