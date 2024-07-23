package org.readutf.orchestrator.server.server.store.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.orchestrator.server.server.RegisteredServer
import org.readutf.orchestrator.server.server.store.ServerStore
import org.readutf.orchestrator.shared.game.Game
import org.readutf.orchestrator.shared.game.GameFinderType
import org.readutf.orchestrator.shared.server.Server
import org.readutf.orchestrator.shared.server.ServerHeartbeat
import java.util.*

class MemoryServerStore : ServerStore {
    private val logger = KotlinLogging.logger { }

    private val servers = mutableMapOf<UUID, RegisteredServer>()
    private val channelServers = mutableMapOf<String, MutableList<UUID>>()

    override fun getServerById(serverId: UUID): RegisteredServer? = servers[serverId]

    override fun saveServer(registeredServer: RegisteredServer) {
        servers[registeredServer.server.serverId] = registeredServer
        channelServers
            .getOrPut(registeredServer.channel.channelId) { mutableListOf() }
            .add(registeredServer.server.serverId)

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
            it.server.heartbeat = serverHeartbeat
        }
    }

    override fun setGames(
        serverId: UUID,
        games: List<Game>,
    ) {
        val serverById = getServerById(serverId)
        serverById?.let {
            it.server.activeGames.clear()
            it.server.activeGames.addAll(games)
        }
    }

    override fun findGamesByType(gameType: String): Map<Server, List<Game>> =
        servers
            .map {
                it.value.server to
                    it.value.server.activeGames
                        .filter { game -> game.matchType == gameType }
            }.toMap()

    override fun findExistingGamesForSearch(gameType: String): Map<Server, List<Game>> {
        val serverToGames = mutableMapOf<Server, MutableList<Game>>()

        servers.values.forEach { registeredServer ->
            if (!registeredServer.server.gameFinders.contains(GameFinderType.PRE_EXISTING)) return@forEach

            val emptyGames =
                registeredServer.server.activeGames
                    .filter { it.matchType == gameType }
                    .filter { it.teams.flatten().isEmpty() }

            if (emptyGames.isNotEmpty()) {
                val games = serverToGames.getOrPut(registeredServer.server) { mutableListOf() }
                games.addAll(emptyGames)
            }
        }

        return serverToGames
    }

    override fun getTimedOutServers(): List<RegisteredServer> {
        val now = System.currentTimeMillis()
        return servers.values.filter { it.server.heartbeat.timestamp < now - 15000 }
    }
}
