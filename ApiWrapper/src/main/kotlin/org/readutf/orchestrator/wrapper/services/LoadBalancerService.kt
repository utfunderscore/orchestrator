package org.readutf.orchestrator.wrapper.services

import org.readutf.orchestrator.shared.server.Server
import org.readutf.orchestrator.shared.utils.ApiResponse
import org.readutf.orchestrator.wrapper.utils.JsonWrapper
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.UUID

interface LoadBalancerService {
    @PUT("/loadbalancer/lobby/setMinimumFillCapacity/{serverType}")
    suspend fun setLobbyBalancer(
        @Path("serverType") serverType: String,
        @Query("capacity") capacity: Float,
    ): ApiResponse<Unit>

    @GET("/loadbalancer/lobby/{serverType}")
    suspend fun getServerFromBalancer(
        @Path("serverType") serverType: String,
        @Query("players") players: JsonWrapper<List<UUID>>,
    ): ApiResponse<Server>
}
