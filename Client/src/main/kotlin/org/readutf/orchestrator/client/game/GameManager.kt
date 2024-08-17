package org.readutf.orchestrator.client.game

import org.readutf.orchestrator.client.network.ClientNetworkManager
import org.readutf.orchestrator.client.network.listeners.GameRequestListener
import org.readutf.orchestrator.client.network.listeners.GameReserveListener
import org.readutf.orchestrator.shared.game.Game
import org.readutf.orchestrator.shared.game.GameReservation
import org.readutf.orchestrator.shared.game.GameState
import org.readutf.orchestrator.shared.packets.ServerGamesUpdatePacket
import org.readutf.orchestrator.shared.utils.Result
import java.util.UUID
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class GameManager(
    private val networkManager: ClientNetworkManager,
    val serverId: UUID,
    scheduler: ScheduledExecutorService,
    val gameRequestHandler: GameRequestHandler?,
) {
    val games = mutableMapOf<UUID, Game>()

    init {
        scheduler.scheduleAtFixedRate(
            { updateGames() },
            0,
            5,
            TimeUnit.SECONDS,
        )
        networkManager.registerListener(GameRequestListener(this))
        networkManager.registerListener(GameReserveListener(this))
    }

    fun registerGame(game: Game) {
        games[game.id] = game
    }

    fun registerGame(
        id: UUID,
        matchType: String,
        teams: List<List<UUID>>,
        gameState: GameState,
    ) {
        registerGame(
            Game(
                id = id,
                serverId = serverId,
                matchType = matchType,
                teams = teams,
                reservation = null,
                gameState = gameState,
            ),
        )
    }

    fun unregisterGame(game: Game) {
        games.remove(game.id)
    }

    fun reserveGame(
        gameId: UUID,
        reservationId: UUID,
    ): Result<GameReservation> {
        val foundGame = games[gameId] ?: return Result.error("Specified game could not be found.")

        if (foundGame.isReserved()) {
            return Result.error("Specified game is already reserved")
        }

        val reservation =
            GameReservation(
                reservationId,
                System.currentTimeMillis(),
                System.currentTimeMillis() + 5000,
            )

        foundGame.reservation =
            reservation

        return Result.ok(reservation)
    }

    fun updateGames() {
        networkManager.sendPacket(
            ServerGamesUpdatePacket(
                serverId,
                games.values.toList(),
            ),
        )
    }

    fun shutdown() {
    }
}
