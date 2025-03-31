package org.readutf.orchestrator.server.api

import com.github.michaelbull.result.Err
import io.javalin.http.Context
import io.javalin.http.Handler
import org.readutf.orchestrator.common.template.TemplateName
import org.readutf.orchestrator.server.service.template.TemplateManager
import org.readutf.orchestrator.server.utils.result

class TemplateEndpoints(val templateManager: TemplateManager) {

    // (POST) /api/template/{name}
    val templateCreateHandler = object : Handler {
        override fun handle(ctx: Context) {
            val name = TemplateName(ctx.pathParam("name"))

            if (templateManager.exists(name)) {
                ctx.result(Err("Template already exists with that name"))
                return
            }

            val body: TemplateCreateBody = ctx.bodyAsClass(TemplateCreateBody::class.java)

            ctx.result(templateManager.update(name, body.image, body.ports, body.environmentVariables))
        }
    }

    // PUT /api/template/{name}
    val templateUpdateHandler = object : Handler {
        override fun handle(ctx: Context) {
            val name = TemplateName(ctx.pathParam("name"))

            if (!templateManager.exists(name)) {
                ctx.result(Err("Template does not exists with that name"))
                return
            }

            val body = ctx.bodyAsClass(TemplateCreateBody::class.java)

            ctx.result(templateManager.update(name, body.image, body.ports, body.environmentVariables))
        }
    }

    // GET /api/template/{name}
    val templateGetHandler = object : Handler {
        override fun handle(ctx: Context) {
            val name = TemplateName(ctx.pathParam("name"))

            if (!templateManager.exists(name)) {
                ctx.result(Err("Template does not exists with that name"))
                return
            }

            ctx.result(templateManager.getOrLoad(name, false))
        }
    }
}
