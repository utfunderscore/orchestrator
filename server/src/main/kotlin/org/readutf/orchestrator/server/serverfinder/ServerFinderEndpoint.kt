package org.readutf.orchestrator.server.serverfinder

import com.github.michaelbull.result.get
import com.github.michaelbull.result.getOrElse
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
    inner class ServerFinderSocket : Consumer<WsConfig> {
        override fun accept(config: WsConfig) {
            config.onConnect { session ->

                val serverType = session.pathParam("type")
                val template =
                    containerController
                        .getTemplate(serverType)
                        .getOrElse {
                            session.send("Invalid Server Type")
                            session.closeSession()
                            return@onConnect
                        }

                val id = template.templateId

                val future = serverFinderManager.findServer(id, Orchestrator.objectMapper.createObjectNode())

                future.thenAccept { serverResult ->
                    if (serverResult.isOk) {
                        session.sendAsClass(ApiResponse.success(serverResult.get()!!.serverId))
                    } else {
                        session.sendAsClass(ApiResponse.error<UUID>())
                    }
                }
            }
        }
    }
}
