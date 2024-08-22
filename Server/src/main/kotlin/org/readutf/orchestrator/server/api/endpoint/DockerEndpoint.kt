package org.readutf.orchestrator.server.api.endpoint

import com.alibaba.fastjson.JSON
import io.javalin.community.routing.annotations.Endpoints
import io.javalin.community.routing.annotations.Get
import io.javalin.community.routing.annotations.Query
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
        @Query("shortId") shortId: String,
    ) {
        val containerByShortId = dockerManager.getContainerByShortId(shortId)

        if (containerByShortId.isError()) {
            context.json(JSON.toJSONString(ApiResponse.failure<String>(containerByShortId.getError())))
        }

        context.json(JSON.toJSONString(ApiResponse.success(containerByShortId.get().getPorts())))
    }
}
