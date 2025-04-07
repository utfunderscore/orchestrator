package org.readutf.orchestrator.server.api

import com.fasterxml.jackson.databind.JsonNode
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.onSuccess
import io.javalin.http.Context
import io.javalin.http.Handler
import org.readutf.orchestrator.common.template.TemplateName
import org.readutf.orchestrator.server.loadbalancer.AutoscaleManager
import org.readutf.orchestrator.server.service.template.TemplateManager
import org.readutf.orchestrator.server.utils.result

class AutoscalerEndpoints(
    val autoscaleManager: AutoscaleManager,
    val templateManager: TemplateManager,
) {

    val setAutoscalerEndpoint = object : Handler {
        override fun handle(ctx: Context) {
            val name = TemplateName(ctx.pathParam("name"))
            val type = ctx.queryParam("type") ?: run {
                ctx.result(Err(Throwable("Missing name parameter")))
                return
            }
            if (!(templateManager.exists(name))) {
                ctx.result(Err(Throwable("Template does not exists with that name")))
                return
            }

            ctx.result(
                autoscaleManager.createScaler(type, ctx.bodyAsClass(JsonNode::class.java)).onSuccess {
                    autoscaleManager.setScaler(name, it)
                },
            )
        }
    }
}
