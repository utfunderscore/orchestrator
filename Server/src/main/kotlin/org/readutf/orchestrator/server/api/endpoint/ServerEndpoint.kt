package org.readutf.orchestrator.server.api.endpoint

import io.javalin.http.Handler
import org.readutf.orchestrator.server.server.ServerManager

object ServerEndpoint {
    fun getServersEndpoint(serverManager: ServerManager): Handler =
        Handler { ctx ->
            ctx.json(serverManager.getAllServers())
        }
}
