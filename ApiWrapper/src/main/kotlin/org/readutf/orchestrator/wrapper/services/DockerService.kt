package org.readutf.orchestrator.wrapper.services

import org.readutf.orchestrator.shared.utils.ApiResponse
import org.readutf.orchestrator.wrapper.types.NetworkAddress
import retrofit2.http.GET
import retrofit2.http.Query

interface DockerService {
    @GET("/docker/port")
    suspend fun getPort(
        @Query("shortId") shortId: String,
    ): String

    @GET("/docker/ip")
    suspend fun getIp(
        @Query("shortId") shortId: String,
    ): ApiResponse<List<NetworkAddress>>
}
