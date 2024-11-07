package org.readutf.orchestrator.wrapper.services

import org.readutf.orchestrator.shared.server.Server
import org.readutf.orchestrator.shared.utils.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ServerService {
    @GET("/server/list")
    suspend fun getAllServers(): ApiResponse<List<Server>>

    @GET("/server/byId")
    suspend fun getServer(
        @Query("serverId") serverId: String,
    ): ApiResponse<Server>

    @GET("/server/byType")
    suspend fun getServerByType(
        @Query("serverType") serverType: String,
    ): ApiResponse<List<Server>>
}
