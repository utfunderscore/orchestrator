package org.readutf.orchestrator.server.api

import com.fasterxml.jackson.databind.JsonNode
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

    val templateDeleteHandler = object : Handler {

        override fun handle(ctx: Context) {
            val name = TemplateName(ctx.pathParam("name"))

            if (!templateManager.exists(name)) {
                ctx.result(Err("Template does not exists with that name"))
                return
            }

            templateManager.delete(name)
            ctx.result(Ok(Unit))
        }
    }

    val addPortHandler = object : Handler {
        override fun handle(ctx: Context) {
            val name = TemplateName(ctx.pathParam("name"))
            val body: JsonNode = ctx.bodyAsClass(JsonNode::class.java)

            val port = body.get("port")?.asInt() ?: run {
                ctx.result(Err(Throwable("Field 'port' missing from body.")))
                return
            }

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
            val body: JsonNode = ctx.bodyAsClass(JsonNode::class.java)

            val port = body.get("port")?.asInt() ?: run {
                ctx.result(Err(Throwable("Field 'port' missing from body.")))
                return
            }

            ctx.result(templateManager.removePort(name, port))
        }
    }

    val setImageHandler = object : Handler {
        override fun handle(ctx: Context) {
            val name = TemplateName(ctx.pathParam("name"))
            val body = ctx.bodyAsClass(JsonNode::class.java)
            val image = body.get("image")?.asText() ?: run {
                ctx.result(Err(Throwable("Field 'image' missing from body.")))
                return
            }

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
            val body = ctx.bodyAsClass(JsonNode::class.java)

            val key = body.get("key")?.asText() ?: run {
                ctx.result(Err(Throwable("Field 'key' missing from body.")))
                return
            }

            val value = body.get("value")?.asText() ?: run {
                ctx.result(Err(Throwable("Field 'value' missing from body.")))
                return
            }

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
            val body = ctx.bodyAsClass(JsonNode::class.java)

            val key = body.get("key")?.asText() ?: run {
                ctx.result(Err(Throwable("Field 'key' missing from body.")))
                return
            }

            if (!templateManager.exists(name)) {
                ctx.result(Err("Template does not exists with that name"))
                return
            }

            ctx.result(templateManager.removeEnvironmentVariable(name, key))
        }
    }
}
