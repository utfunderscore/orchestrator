package org.readutf.orchestrator.server.loadbalancer.impl

import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.shared.server.Server
import org.readutf.orchestrator.shared.utils.Result
import java.util.UUID
import kotlin.math.max

open class LobbyLoadBalancer(
    private val serverManager: ServerManager,
    private val serverType: String,
    var minimumFillCapacity: Float,
) {
    init {
        require(minimumFillCapacity in 0.0..1.0) { "Minimum fill capacity must be between 0.0 and 1.0" }
    }

    @Synchronized
    fun request(players: List<UUID>): Result<Server, String> {
        val servers = serverManager.getServersByType(serverType)

        val targetServer =
            servers
                .asSequence()
                .map { it to max(it.heartbeat.capacity, minimumFillCapacity) }
                .sortedWith(compareBy({ it.second }, { it.first.getUptime() }))
                .firstOrNull() ?: return Result.failure("No servers available")

        val server = targetServer.first

        serverManager.recalculateCapacity(server, players.size, false, null)

        return Result.success(server)
    }
}
