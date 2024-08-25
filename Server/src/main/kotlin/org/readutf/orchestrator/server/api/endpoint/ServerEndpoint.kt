package org.readutf.orchestrator.server.api.endpoint

import io.javalin.community.routing.annotations.Endpoints
import io.javalin.community.routing.annotations.Get
import io.javalin.community.routing.annotations.Query
import io.javalin.http.Context
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.shared.server.Server
import org.readutf.orchestrator.shared.utils.ApiResponse
import java.util.UUID

@Endpoints("/server")
class ServerEndpoint(
    val serverManager: ServerManager,
) {
    @Get("list")
    fun listServers(context: Context): ApiResponse<List<Server>> = ApiResponse.success(serverManager.getAllServers() as List<Server>)

    @Get("byId")
    fun getServer(
        context: Context,
        @Query("serverId") serverIdString: String,
    ): ApiResponse<Server> {
        val serverId =
            try {
                UUID.fromString(serverIdString)
            } catch (e: Exception) {
                return ApiResponse.failure("Invalid uuid 'serverId'")
            }

        return serverManager.getServerById(serverId)?.let { server ->
            ApiResponse.success(server)
        } ?: let { _ ->
            ApiResponse.failure("Could not find server with that id")
        }
    }

    @Get("byType")
    fun getServerByType(
        context: Context,
        @Query("serverType") gameType: String,
    ): ApiResponse<List<Server>> = ApiResponse.success(serverManager.getServersByType(gameType))
}
