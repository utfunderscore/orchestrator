package org.readutf.orchestrator.server.api.endpoint

import com.alibaba.fastjson.JSON
import io.javalin.community.routing.annotations.Endpoints
import io.javalin.community.routing.annotations.Get
import io.javalin.community.routing.annotations.Query
import io.javalin.http.Context
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
        val containerByShortId = dockerManager.getContainerByShortId(shortId)

        if (containerByShortId.isError()) {
            context.json(
                Base64.getEncoder().encodeToString(
                    JSON.toJSONString(ApiResponse.failure<String>(containerByShortId.getError())).toByteArray(),
                ),
            )
            return
        }

        context.json(
            Base64.getEncoder().encodeToString(
                JSON
                    .toJSONString(ApiResponse.success(containerByShortId.get().getPorts()))
                    .toByteArray(),
            ),
        )
    }
}
