package org.readutf.orchestrator.server.loadbalancer.endpoint

import com.fasterxml.jackson.module.kotlin.readValue
import io.javalin.community.routing.annotations.*
import io.javalin.http.Context
import org.readutf.orchestrator.server.Orchestrator
import org.readutf.orchestrator.server.loadbalancer.LoadBalanceManager
import org.readutf.orchestrator.shared.server.Server
import org.readutf.orchestrator.shared.utils.ApiResponse
import org.readutf.orchestrator.shared.utils.catch
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
        @Query players: String,
    ): ApiResponse<Server> {
        val playerIds =
            catch {
                Orchestrator.objectMapper.readValue<List<UUID>>(players)
            }.onFailure { return ApiResponse.failure("Invalid json for parameter 'players'") }

        return loadBalanceManager.getLobbyBalancer(serverType).request(playerIds).toApiResponse()
    }
}
