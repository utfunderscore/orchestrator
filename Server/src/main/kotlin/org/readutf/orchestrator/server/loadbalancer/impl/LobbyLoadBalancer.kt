package org.readutf.orchestrator.server.loadbalancer.impl

import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.server.server.scalable.ServerScaleManager
import org.readutf.orchestrator.shared.server.Server
import org.readutf.orchestrator.shared.utils.Result
import java.util.UUID
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

open class LobbyLoadBalancer(
    private val serverManager: ServerManager,
    private val scaleManager: ServerScaleManager,
    private val serverType: String,
    var minimumFillCapacity: Float,
    val capacityIncreaseThreshhold: Float = 0.8f,
    val capacityDecreaseThreshhold: Float = 0.2f,
) {
    init {
        require(minimumFillCapacity in 0.0..1.0) { "Minimum fill capacity must be between 0.0 and 1.0" }

        val targetScale = scaleManager.getTargetScale(serverType)
        if (targetScale < 1) scaleManager.setScale(serverType, 1)
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

        serverManager.recalculateCapacity(server, players.size, false, null).thenAccept {
            scaleManager.setScale(serverType, getTargetCapacity(servers))
        }

        return Result.success(server)
    }

    private fun getTargetCapacity(servers: List<Server>): Int {
        val totalCapacity = servers.sumOf { it.heartbeat.capacity.toDouble() }

        val roundedDownCapacity = floor(totalCapacity - 0.1).toInt()
        val numberPart = totalCapacity - floor(totalCapacity)
        val currentCapacity = servers.size

        println("Total capacity: $totalCapacity")
        println("Max Capacity: $currentCapacity")
        println("Rounded down capacity: $roundedDownCapacity")
        println("Number part: $numberPart")

        if (numberPart < 0.3) {
            return max(1, min(currentCapacity, roundedDownCapacity))
        }

        if (numberPart > 0.7) {
            return max(1, min(currentCapacity, roundedDownCapacity + 1))
        }

        return max(1, min(currentCapacity, roundedDownCapacity + 2))
    }
}
