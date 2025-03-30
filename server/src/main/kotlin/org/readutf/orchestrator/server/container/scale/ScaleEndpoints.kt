package org.readutf.orchestrator.server.container.scale

import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.runCatching
import io.javalin.http.Context
import org.readutf.orchestrator.server.utils.result

class ScaleEndpoints(
    val scaleManager: ScaleManager,
) {
    fun scaleServer(context: Context) {
        val id = context.pathParam("id")
        val scale = context.queryParam("scale")

        context.result(
            runCatching {
                Integer.parseInt(scale)
            }.andThen { scaleInt ->
                scaleManager.scaleDeployment(id, scaleInt)
            },
        )
    }
}
