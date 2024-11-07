package org.readutf.orchestrator.server.loadbalancer.endpoint

import io.javalin.community.routing.annotations.*
import io.javalin.http.Context
import org.readutf.orchestrator.server.loadbalancer.LoadBalanceManager
import org.readutf.orchestrator.shared.server.Server
import org.readutf.orchestrator.shared.utils.ApiResponse
import org.readutf.orchestrator.shared.utils.toApiResponse
import java.util.UUID

@Endpoints("/loadbalancer")
class LoadbalancerEndpoints(
    val loadBalanceManager: LoadBalanceManager,
) {
    @Put("/lobby/setMinimumFillCapacity/{serverType}")
    fun setLobbyBalancer(
        context: Context,
        @Param serverType: String,
        @Query capacity: Float,
    ): ApiResponse<Unit> = loadBalanceManager.setMinimumFillCapacity(serverType, capacity).toApiResponse()

    @Get("/lobby/{serverType}")
    fun getServerFromBalancer(
        context: Context,
        @Param serverType: String,
        @Body players: List<UUID>,
    ): ApiResponse<Server> = loadBalanceManager.getLobbyBalancer(serverType).request(players).toApiResponse()
}
