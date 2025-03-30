package org.readutf.orchestrator.server.server

import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.runCatching
import io.javalin.http.Context
import org.readutf.orchestrator.server.utils.result

class ServerEndpoints(
    val serverManager: ServerManager,
) {
    fun listServers(context: Context) {
        context.result(
            runCatching {
                serverManager.getServers()
            }.onFailure {
                println("Failed to find servers: $it")
            },
        )
    }
}
