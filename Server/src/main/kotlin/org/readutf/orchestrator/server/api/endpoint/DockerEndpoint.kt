package org.readutf.orchestrator.server.api.endpoint

import io.javalin.community.routing.annotations.Endpoints
import io.javalin.community.routing.annotations.Get
import io.javalin.http.Context
import org.readutf.orchestrator.server.docker.DockerManager
import org.readutf.orchestrator.shared.utils.ApiResponse

@Endpoints("/docker")
class DockerEndpoint(
    val dockerManager: DockerManager,
) {
    @Get("port")
    fun getPort(
        context: Context,
        shortId: String,
    ) {
        val containerByShortId = dockerManager.getContainerByShortId(shortId)

        if (containerByShortId.isError()) {
            context.json(ApiResponse.failure<String>(containerByShortId.getError()))
        }

        context.json(ApiResponse.success(containerByShortId.get().getPorts()))
    }
}
