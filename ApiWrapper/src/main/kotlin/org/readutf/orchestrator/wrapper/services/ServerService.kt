package org.readutf.orchestrator.wrapper.services

import org.readutf.orchestrator.shared.server.Server
import org.readutf.orchestrator.shared.utils.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.UUID

interface ServerService {
    @GET("/server/list")
    suspend fun getAllServers(): ApiResponse<List<Server>>

    @GET("/server/byId")
    suspend fun getServer(
        @Query("serverId") serverId: UUID,
    ): ApiResponse<Server>

    @GET("/server/byType")
    suspend fun getServer(
        @Query("serverType") serverType: String,
    ): ApiResponse<List<Server>>
}
