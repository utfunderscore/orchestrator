package org.readutf.orchestrator.server.container

import com.fasterxml.jackson.databind.JsonNode
import com.github.michaelbull.result.runCatching
import io.javalin.http.Context
import io.javalin.http.Handler
import org.readutf.orchestrator.server.utils.result

class ContainerEndpoints(
    private val containerManager: ContainerManager<*>,
) {

    // (PUT) /api/service/{name}
    val createServiceEndpoint = object : Handler {
        override fun handle(ctx: Context) {
            val name = ctx.pathParam("name")
            val node = ctx.bodyAsClass(JsonNode::class.java)

            var result = containerManager.createTemplate(name, node)

            ctx.result(result)
        }
    }

    val getTemplatesEndpoint = object : Handler {
        override fun handle(ctx: Context) {
            ctx.result(runCatching { containerManager.getTemplates() })
        }
    }

    val deleteServiceEndpoint = object : Handler {
        override fun handle(ctx: Context) {
            val name = ctx.pathParam("name")

            ctx.result(containerManager.deleteTemplate(name))
        }
    }
}
