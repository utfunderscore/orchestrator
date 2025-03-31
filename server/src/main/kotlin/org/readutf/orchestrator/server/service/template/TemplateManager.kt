package org.readutf.orchestrator.server.service.template

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onSuccess
import org.readutf.orchestrator.common.template.ServiceTemplate
import org.readutf.orchestrator.common.template.TemplateName
import org.readutf.orchestrator.server.service.template.store.TemplateStore

class TemplateManager(private val templateStore: TemplateStore) {

    private val inMemoryCache = mutableMapOf<TemplateName, ServiceTemplate>()

    fun update(
        id: TemplateName,
        image: String,
        ports: List<Int>,
        environmentVariables: Map<String, String>,
    ): Result<ServiceTemplate, Throwable> {
        val template = ServiceTemplate(
            name = id,
            image = image,
            ports = ports.toHashSet(),
            environmentVariables = environmentVariables,
        )

        templateStore.update(id, image, ports, environmentVariables)
        inMemoryCache[id] = template

        return Ok(template)
    }

    fun exists(
        id: TemplateName,
    ): Boolean {
        if (inMemoryCache.contains(id)) return true

        return templateStore.exists(id)
    }

    fun getOrLoad(
        templateName: TemplateName,
        cache: Boolean = true,
    ): Result<ServiceTemplate, Throwable> {
        inMemoryCache[templateName]?.let {
            return Ok(it)
        }

        return templateStore.load(templateName).onSuccess {
            if (cache) {
                inMemoryCache[templateName] = it
            }
        }
    }
}
