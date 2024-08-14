package org.readutf.orchestrator.server.api

import io.javalin.Javalin
import org.readutf.orchestrator.server.api.endpoint.ServerEndpoint
import org.readutf.orchestrator.server.game.GameManager
import org.readutf.orchestrator.server.game.endpoints.GameRequestSocket
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.server.settings.ApiSettings
import org.readutf.orchestrator.server.utils.FastJsonMapper

class EndpointManager(
    val apiSettings: ApiSettings,
    val serverManager: ServerManager,
    val gameManager: GameManager,
) {
    private val javalin = setupJavalin()

    init {

        javalin.ws(
            "/game/request",
            GameRequestSocket(
                gameManager = gameManager,
                serverManager = serverManager,
            ),
        )

        javalin.get("/server/list", ServerEndpoint.getServersEndpoint(serverManager))
    }

    fun shutdown() {
        javalin.stop()
    }

    private fun setupJavalin() =
        Javalin.createAndStart {
            it.jetty.defaultHost = apiSettings.host
            it.jetty.defaultPort = apiSettings.port
            it.jsonMapper(FastJsonMapper)
            it.useVirtualThreads = apiSettings.virtualThreads
            it.showJavalinBanner = false
        }
}
