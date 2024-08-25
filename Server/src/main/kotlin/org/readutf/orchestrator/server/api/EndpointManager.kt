package org.readutf.orchestrator.server.api

import io.javalin.Javalin
import io.javalin.community.routing.annotations.AnnotatedRouting.Annotated
import io.javalin.community.routing.annotations.HandlerResultConsumer
import org.readutf.orchestrator.server.api.endpoint.DockerEndpoint
import org.readutf.orchestrator.server.api.endpoint.GameRequestSocket
import org.readutf.orchestrator.server.api.endpoint.ServerEndpoint
import org.readutf.orchestrator.server.docker.DockerManager
import org.readutf.orchestrator.server.game.GameManager
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.server.settings.Settings
import org.readutf.orchestrator.server.utils.FastJsonMapper
import org.readutf.orchestrator.shared.utils.ApiResponse

class EndpointManager(
    val settings: Settings,
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
            config.jetty.defaultHost = settings.apiSettings.host
            config.jetty.defaultPort = settings.apiSettings.port
            config.jsonMapper(FastJsonMapper)
            config.useVirtualThreads = settings.apiSettings.virtualThreads
            config.showJavalinBanner = false
            config.bundledPlugins.enableDevLogging()

            // register endpoints
            config.router.mount(Annotated) { routing ->
                routing.registerEndpoints(ServerEndpoint(serverManager))
                routing.registerEndpoints(DockerEndpoint(DockerManager(settings.dockerSettings)))

                routing.registerResultHandler(
                    ApiResponse::class.java,
                    HandlerResultConsumer { ctx, value -> ctx.json(value) },
                )
            }

            config.pvt.internalRouter.allHttpHandlers().forEach { parsedEndpoint ->
                val endpoint = parsedEndpoint.endpoint
                println("Registered ${endpoint.method.name} endpoint '${endpoint.path}")
            }
        }
}
