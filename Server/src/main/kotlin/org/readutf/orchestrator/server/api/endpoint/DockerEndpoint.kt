package org.readutf.orchestrator.server.api.endpoint

import io.javalin.community.routing.annotations.Endpoints
import io.javalin.community.routing.annotations.Get
import io.javalin.community.routing.annotations.Query
import io.javalin.http.Context
import org.readutf.orchestrator.server.Orchestrator
import org.readutf.orchestrator.server.docker.DockerManager
import org.readutf.orchestrator.shared.utils.ApiResponse
import java.util.Base64

@Endpoints("/docker")
class DockerEndpoint(
    val dockerManager: DockerManager,
) {
    @Get("port")
    fun getPort(
        context: Context,
        @Query("shortId") shortId: String,
    ) {
        val containerByShortId =
            dockerManager.getContainerByShortId(shortId).onFailure {
                context.json(
                    Base64.getEncoder().encodeToString(
                        Orchestrator.objectMapper
                            .writeValueAsString(
                                ApiResponse.failure<String>(it.getError()),
                            ).toByteArray(),
                    ),
                )
                return
            }

        context.json(
            Base64.getEncoder().encodeToString(
                Orchestrator.objectMapper
                    .writeValueAsString(ApiResponse.success(containerByShortId.getPorts()))
                    .toByteArray(),
            ),
        )
    }

    @Get("ip")
    fun getIp(
        context: Context,
        @Query("shortId") shortId: String,
    ) {
        val containerByShortId = dockerManager.getContainerByShortId(shortId)

        if (containerByShortId.isFailure) {
            context.json(
                Orchestrator.objectMapper
                    .writeValueAsString(
                        ApiResponse.failure<String>(containerByShortId.getError()),
                    ).toByteArray(),
            )
            return
        }

        val container = containerByShortId.getOrNull() ?: return
        val networkSettings = container.networkSettings ?: return

        val networks =
            networkSettings.networks.map { (name, network) ->
                mapOf(
                    "network" to name,
                    "ip" to network.ipAddress,
                )
            }

        context.json(ApiResponse.success(networks))
    }
}
