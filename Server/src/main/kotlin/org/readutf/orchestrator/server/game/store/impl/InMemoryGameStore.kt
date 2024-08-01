package org.readutf.orchestrator.server.game.store.impl

import org.readutf.orchestrator.server.game.store.GameStore
import org.readutf.orchestrator.server.server.RegisteredServer
import org.readutf.orchestrator.server.server.store.impl.MemoryServerStore
import org.readutf.orchestrator.shared.game.Game
import org.readutf.orchestrator.shared.game.GameFinderType
import org.readutf.orchestrator.shared.game.GameState
import java.util.*

class InMemoryGameStore(
    private val serverStore: MemoryServerStore,
) : GameStore {
    private val idToGame = mutableMapOf<UUID, Game>()
    private val serverToGames = mutableMapOf<UUID, MutableList<Game>>()

    override fun save(game: Game) {
        idToGame[game.id] = game
        serverToGames
            .getOrPut(game.serverId) { mutableListOf() }
            .add(game)
    }

    override fun remove(game: Game) {
        idToGame.remove(game.id)
        serverToGames[game.serverId]?.remove(game)
    }

    override fun getGameById(gameId: UUID): Game? = idToGame[gameId]

    override fun getGamesByServerId(serverId: UUID): List<Game> = serverToGames[serverId] ?: emptyList()

    override fun findEmptyExistingGames(gameType: String): List<Pair<RegisteredServer, Game>> {
        val serverGame = mutableListOf<Pair<RegisteredServer, Game>>()

        serverToGames.forEach { (server: UUID, games: MutableList<Game>) ->
            val serverById = serverStore.getServerById(server) ?: return@forEach
            if (!serverById.gameFinders.contains(GameFinderType.PRE_EXISTING)) return@forEach

            serverGame.addAll(
                games
                    .filter { it.matchType == gameType }
                    .filter { it.gameState == GameState.IDLE }
                    .filter { it.teams.flatten().isEmpty() }
                    .map { (serverById to it) },
            )
        }

        return serverGame
    }

    override fun setGames(
        serverId: UUID,
        games: List<Game>,
    ) {
        serverToGames[serverId] = games.toMutableList()
    }

    override fun findGameRequestServers(sortByActiveGames: Boolean): List<RegisteredServer> {
        val onRequestServers = serverStore.servers.values.filter { it.gameFinders.contains(GameFinderType.ON_REQUEST) }

        return onRequestServers
            .takeIf { sortByActiveGames }
            ?.sortedBy { getGamesByServerId(it.serverId).size }
            ?: onRequestServers
    }
}
