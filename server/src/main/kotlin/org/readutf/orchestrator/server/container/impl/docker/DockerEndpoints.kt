package org.readutf.orchestrator.server.container.impl.docker

import com.github.michaelbull.result.*
import io.javalin.http.Context
import org.readutf.orchestrator.common.utils.result
import org.readutf.orchestrator.server.container.impl.docker.store.DockerTemplateStore

class DockerEndpoints(
    private val dockerTemplateStore: DockerTemplateStore,
) {
    fun createTemplate(context: Context) {
        context.result(
            runCatching { context.bodyAsClass(DockerTemplate::class.java) }
                .mapError { "Failed to parse body, invalid json" }
                .andThen { template ->
                    dockerTemplateStore.saveTemplate(template)
                    Ok(template)
                },
        )
    }

    fun listTemplates(context: Context) {
        context.result(
            runCatching { dockerTemplateStore.getTemplates() },
        )
    }

    fun getTemplate(context: Context) {
        val template: Result<DockerTemplate?, String> =
            runCatching {
                context.pathParam("id")
            }.mapError {
                "Missing 'id' path parameter"
            }.andThen { id ->
                dockerTemplateStore.getTemplate(id)
            }

        return context.result(template)
    }
}
