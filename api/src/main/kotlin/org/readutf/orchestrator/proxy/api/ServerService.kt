package org.readutf.orchestrator.proxy.api

import org.readutf.orchestrator.common.server.Server
import retrofit2.http.GET

interface ServerService {

    @GET("/api/server")
    suspend fun getServers(): List<Server>
}
