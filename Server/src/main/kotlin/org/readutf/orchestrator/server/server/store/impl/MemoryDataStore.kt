package org.readutf.orchestrator.server.server.store.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.orchestrator.server.server.RegisteredServer
import org.readutf.orchestrator.server.server.store.DataStore
import org.readutf.orchestrator.shared.game.Game
import org.readutf.orchestrator.shared.game.GameFinderType
import org.readutf.orchestrator.shared.game.GameState
import org.readutf.orchestrator.shared.server.ServerHeartbeat
import java.util.*

class MemoryDataStore : DataStore {
    private val logger = KotlinLogging.logger { }

    private val servers = mutableMapOf<UUID, RegisteredServer>()
    private val channelServers = mutableMapOf<String, MutableList<UUID>>()

    override fun getServerById(serverId: UUID): RegisteredServer? = servers[serverId]

    override fun saveServer(registeredServer: RegisteredServer) {
        servers[registeredServer.serverId] = registeredServer
        channelServers
            .getOrPut(registeredServer.channel.channelId) { mutableListOf() }
            .add(registeredServer.serverId)

        println(channelServers)
    }

    override fun removeServer(serverId: UUID) {
        servers.remove(serverId)
    }

    override fun getServersByChannel(channelId: String): List<RegisteredServer> =
        channelServers[channelId]?.mapNotNull { servers[it] } ?: emptyList()

    override fun updateHeartbeat(
        serverId: UUID,
        serverHeartbeat: ServerHeartbeat,
    ) {
        servers[serverId]?.let {
            it.heartbeat = serverHeartbeat
        }
    }

    override fun setGames(
        serverId: UUID,
        games: List<Game>,
    ) {
        val serverById = getServerById(serverId)
        serverById?.let {
            it.activeGames.clear()
            it.activeGames.addAll(games)
        }
    }

    override fun findEmptyExistingGames(gameType: String): List<Pair<RegisteredServer, Game>> {
        val serverToGames = mutableListOf<Pair<RegisteredServer, Game>>()

        servers.values.forEach { registeredServer ->
            if (!registeredServer.gameFinders.contains(GameFinderType.PRE_EXISTING)) return@forEach

            val emptyGames =
                registeredServer.activeGames
                    .filter { it.matchType == gameType }
                    .filter { it.gameState == GameState.IDLE }
                    .filter { it.teams.flatten().isEmpty() }

            if (emptyGames.isNotEmpty()) {
                serverToGames.add(registeredServer to emptyGames.first())
            }
        }

        return serverToGames
    }

    override fun getTimedOutServers(): List<RegisteredServer> {
        val now = System.currentTimeMillis()
        return servers.values.filter { it.heartbeat.timestamp < now - 15000 }
    }
}
