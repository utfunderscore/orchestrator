package org.readutf.orchestrator.server.api

import io.javalin.Javalin
import io.javalin.community.routing.annotations.AnnotatedRouting.Annotated
import org.readutf.orchestrator.server.api.endpoint.GameRequestSocket
import org.readutf.orchestrator.server.api.endpoint.ServerEndpoint
import org.readutf.orchestrator.server.game.GameManager
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.server.settings.ApiSettings
import org.readutf.orchestrator.server.utils.FastJsonMapper

class EndpointManager(
    val apiSettings: ApiSettings,
    val serverManager: ServerManager,
    val gameManager: GameManager,
) {
    private val javalin = setupJavalin()

//    private val logger = KotlinLogging.logger { }

    init {

        javalin.ws(
            "/game/request",
            GameRequestSocket(
                gameManager = gameManager,
                serverManager = serverManager,
            ),
        )
    }

    fun shutdown() {
        javalin.stop()
    }

    private fun setupJavalin() =
        Javalin.createAndStart { config ->
            config.jetty.defaultHost = apiSettings.host
            config.jetty.defaultPort = apiSettings.port
            config.jsonMapper(FastJsonMapper)
            config.useVirtualThreads = apiSettings.virtualThreads
            config.showJavalinBanner = false
            config.bundledPlugins.enableDevLogging()

            // register endpoints
            config.router.mount(Annotated) { routing ->
                routing.registerEndpoints(ServerEndpoint(serverManager))
            }

            config.pvt.internalRouter.allHttpHandlers().forEach { parsedEndpoint ->
                val endpoint = parsedEndpoint.endpoint
                println("Registered ${endpoint.method.name} endpoint '${endpoint.path}")
            }
        }
}
