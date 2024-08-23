package org.readutf.orchestrator.wrapper.services

import retrofit2.http.GET
import retrofit2.http.Query

interface DockerService {
    @GET("/docker/port")
    suspend fun getPort(
        @Query("shortId") shortId: String,
    ): String
}
