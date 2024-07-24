package org.readutf.orchestrator.client.game

import org.readutf.orchestrator.client.network.ClientNetworkManager
import org.readutf.orchestrator.client.server.ServerManager
import org.readutf.orchestrator.shared.game.Game
import org.readutf.orchestrator.shared.game.GameState
import org.readutf.orchestrator.shared.packets.ServerGamesUpdatePacket
import java.util.UUID
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class GameManager(
    private val networkManager: ClientNetworkManager,
    private val serverManager: ServerManager,
    scheduler: ScheduledExecutorService,
) {
    private val games = mutableMapOf<UUID, Game>()

    init {
        scheduler.scheduleAtFixedRate(
            { updateGames() },
            0,
            5,
            TimeUnit.SECONDS,
        )
    }

    fun registerGame(
        id: UUID,
        matchType: String,
        teams: List<List<UUID>>,
        gameState: GameState,
    ) {
        games[id] = Game(id, serverManager.serverId, matchType, teams, gameState)
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

    fun updateGames() {
        networkManager.sendPacket(
            ServerGamesUpdatePacket(
                serverManager.serverId,
                games.values.toList(),
            ),
        )
    }
}
