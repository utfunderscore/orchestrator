package org.readutf.orchestrator.server.api

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.toResultOr
import io.javalin.http.Context
import io.javalin.http.Handler
import org.readutf.orchestrator.common.template.TemplateBody
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

            val body: TemplateBody = ctx.bodyAsClass(TemplateBody::class.java)

            ctx.result(templateManager.save(name, body.image, body.ports, body.environmentVariables))
        }
    }

    val addPortHandler = object : Handler {
        override fun handle(ctx: Context) {
            val name = TemplateName(ctx.pathParam("name"))
            val port = ctx.bodyAsClass(Int::class.java)

            if (!templateManager.exists(name)) {
                ctx.result(Err("Template does not exists with that name"))
                return
            }

            ctx.result(templateManager.addPort(name, port))
        }
    }

    val removePortHandler = object : Handler {
        override fun handle(ctx: Context) {
            val name = TemplateName(ctx.pathParam("name"))
            val port = ctx.bodyAsClass(Int::class.java)

            if (!templateManager.exists(name)) {
                ctx.result(Err("Template does not exists with that name"))
                return
            }

            ctx.result(templateManager.removePort(name, port))
        }
    }

    val setImageHandler = object : Handler {
        override fun handle(ctx: Context) {
            val name = TemplateName(ctx.pathParam("name"))
            val image = ctx.bodyAsClass(String::class.java)

            if (!templateManager.exists(name)) {
                ctx.result(Err("Template does not exists with that name"))
                return
            }

            ctx.result(templateManager.setImage(name, image))
        }
    }

    val setEnvironmentVariableHandler = object : Handler {
        override fun handle(ctx: Context) {
            val name = TemplateName(ctx.pathParam("name"))
            val key = ctx.bodyAsClass(String::class.java)
            val value = ctx.bodyAsClass(String::class.java)

            if (!templateManager.exists(name)) {
                ctx.result(Err("Template does not exists with that name"))
                return
            }

            ctx.result(templateManager.setEnvironmentVariable(name, key, value))
        }
    }

    val removeEnvironmentVariableHandler = object : Handler {
        override fun handle(ctx: Context) {
            val name = TemplateName(ctx.pathParam("name"))
            val key = ctx.bodyAsClass(String::class.java)

            if (!templateManager.exists(name)) {
                ctx.result(Err("Template does not exists with that name"))
                return
            }

            ctx.result(templateManager.removeEnvironmentVariable(name, key))
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

            ctx.result(templateManager.get(name).toResultOr { Throwable("No template with that id") })
        }
    }

    val templateListHandler = object : Handler {
        override fun handle(ctx: Context) {
            ctx.result(Ok(templateManager.getAll()))
        }
    }
}
