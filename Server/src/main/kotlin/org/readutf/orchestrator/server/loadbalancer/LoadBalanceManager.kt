package org.readutf.orchestrator.server.loadbalancer

import org.readutf.orchestrator.server.loadbalancer.impl.LobbyLoadBalancer
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.shared.utils.Result
import org.readutf.orchestrator.shared.utils.catch

class LoadBalanceManager(
    val serverManager: ServerManager,
) {
    private val lobbyLoadBalancers =
        mutableMapOf<String, LobbyLoadBalancer>().withDefault {
            LobbyLoadBalancer(
                serverManager,
                it,
                0.1f,
            )
        }

    fun setMinimumFillCapacity(
        serverType: String,
        capacity: Float,
    ): Result<Unit, String> =
        catch {
            require(capacity in 0.0..1.0) { "Minimum fill capacity must be between 0.0 and 1.0" }
            lobbyLoadBalancers.getValue(serverType).minimumFillCapacity = capacity
        }

    fun getLobbyBalancer(serverType: String): LobbyLoadBalancer = lobbyLoadBalancers.getValue(serverType)
}
