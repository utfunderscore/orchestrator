package org.readutf.orchestrator.server.game.store

import org.readutf.orchestrator.server.server.RegisteredServer
import org.readutf.orchestrator.shared.game.Game
import org.readutf.orchestrator.shared.game.GameReservation
import java.util.UUID

interface GameStore {
    fun save(game: Game)

    fun remove(game: Game)

    fun getGameById(gameId: UUID): Game?

    fun getGamesByServerId(serverId: UUID): List<Game>

    fun setReservation(
        gameId: UUID,
        reservation: GameReservation,
    ) {
        getGameById(gameId)?.reservation = reservation
    }

    fun findEmptyExistingGames(gameType: String): List<Pair<RegisteredServer, Game>>

    fun setGames(
        serverId: UUID,
        games: List<Game>,
    )

    fun findGameRequestServers(sortByActiveGames: Boolean): List<RegisteredServer>
}
