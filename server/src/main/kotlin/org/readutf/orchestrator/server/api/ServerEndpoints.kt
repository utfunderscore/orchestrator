package org.readutf.orchestrator.server.api

import com.github.michaelbull.result.Ok
import io.javalin.http.Context
import io.javalin.http.Handler
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.server.utils.result

class ServerEndpoints(serverManager: ServerManager) {

    val getServersEndpoint = object : Handler {
        override fun handle(ctx: Context) = ctx.result(Ok(serverManager.getServers()))
    }
}
