package org.readutf.orchestrator.server.serverfinder

import com.github.michaelbull.result.get
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import io.github.oshai.kotlinlogging.KotlinLogging
import io.javalin.websocket.WsConfig
import org.readutf.orchestrator.common.api.ApiResponse
import org.readutf.orchestrator.server.Orchestrator
import org.readutf.orchestrator.server.container.ContainerController
import java.util.UUID
import java.util.function.Consumer

class ServerFinderEndpoint(
    val serverFinderManager: ServerFinderManager,
    val containerController: ContainerController<*>,
) {
    private val logger = KotlinLogging.logger { }

    inner class ServerFinderSocket : Consumer<WsConfig> {
        override fun accept(config: WsConfig) {
            config.onConnect { session ->

                val serverType = session.pathParam("type")
                val template =
                    containerController
                        .getTemplate(serverType)
                        .getOrElse {
                            logger.error { "Invalid Server Type $serverType" }
                            session.send("Invalid Server Type")
                            session.closeSession()
                            return@onConnect
                        }

                logger.info { "Finding server for template $template" }

                val future = serverFinderManager.findServer(template.templateId, Orchestrator.objectMapper.createObjectNode())

                future.thenAccept { serverResult ->
                    serverResult
                        .onSuccess {
                            logger.info { "Found server ${it.serverId}" }
                            session.sendAsClass(ApiResponse.success(it))
                        }.onFailure {
                            logger.error { "Failed to find server $it" }
                            session.sendAsClass(ApiResponse.error<UUID>("No server found"))
                        }
                }
            }
        }
    }
}
