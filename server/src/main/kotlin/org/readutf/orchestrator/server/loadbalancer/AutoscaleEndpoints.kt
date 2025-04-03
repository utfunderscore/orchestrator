package org.readutf.orchestrator.server.loadbalancer

import com.fasterxml.jackson.databind.JsonNode
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getOrElse
import io.javalin.http.Context
import io.javalin.http.Handler
import org.readutf.orchestrator.common.template.TemplateName
import org.readutf.orchestrator.server.service.template.TemplateManager
import org.readutf.orchestrator.server.utils.result

class AutoscaleEndpoints(
    autoscaleManager: AutoscaleManager,
    templateManager: TemplateManager,
) {

    // POST ("/autoscale/{name}")
    private val setAutoscaler = object : Handler {
        override fun handle(ctx: Context) {
            val name: String = ctx.pathParam("name")
            val type: String? = ctx.queryParam("type")
            val template: String? = ctx.queryParam("template")
            if (type == null) {
                ctx.result(Err(Throwable("Missing 'type' query parameter")))
                return
            }
            if (template == null) {
                ctx.result(Err(Throwable("Missing 'template' query parameter")))
                return
            }

            var body: JsonNode = ctx.bodyAsClass(JsonNode::class.java)

            val serializer = autoscaleManager.getSerializer(type)
            if (serializer == null) {
                ctx.result(Err(Throwable("Unknown serializer type $type")))
                return
            }

            val result = serializer.create(body).getOrElse {
                ctx.result(Err(it))
                return
            }

            var templateName = TemplateName(template)
            templateManager.exists(templateName)

            autoscaleManager.setScaler(templateName, result)

            ctx.result(Ok(result))
        }
    }
}
