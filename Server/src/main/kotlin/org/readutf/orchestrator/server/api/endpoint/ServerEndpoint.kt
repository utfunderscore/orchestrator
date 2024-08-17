package org.readutf.orchestrator.server.api.endpoint

import io.javalin.community.routing.annotations.Endpoints
import io.javalin.community.routing.annotations.Get
import io.javalin.community.routing.annotations.Query
import io.javalin.http.Context
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.shared.utils.ApiResponse
import java.util.UUID

@Endpoints("/server")
class ServerEndpoint(
    val serverManager: ServerManager,
) {
    @Get("list")
    fun listServers(context: Context) {
        context.json(ApiResponse.success(serverManager.getAllServers()))
    }

    @Get("")
    fun getServer(
        context: Context,
        @Query("serverId") serverIdString: String,
    ) {
        val serverId =
            try {
                UUID.fromString(serverIdString)
            } catch (e: Exception) {
                context.json(ApiResponse.failure<String>("Invalid uuid 'serverId'"))
                return
            }

        serverManager.getServerById(serverId)?.let {
            context.json(ApiResponse.success(it))
        } ?: run {
            context.json(ApiResponse.failure<String>("Could not find server with that id"))
        }
    }
}
