package org.readutf.orchestrator.server.service.template

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrThrow
import com.github.michaelbull.result.runCatching
import org.readutf.orchestrator.common.template.ServiceTemplate
import org.readutf.orchestrator.common.template.TemplateName
import org.readutf.orchestrator.server.service.template.store.TemplateStore

class TemplateManager(private val templateStore: TemplateStore) {

    private val inMemoryCache = templateStore.load().getOrThrow().associateBy { it.name }.toMutableMap()

    fun save(
        id: TemplateName,
        image: String,
        ports: List<Int>,
        environmentVariables: HashMap<String, String>,
    ): Result<ServiceTemplate, Throwable> {
        val template = ServiceTemplate(
            name = id,
            image = image,
            ports = ports.toHashSet(),
            environmentVariables = environmentVariables,
        )

        templateStore.save(id, image, ports, environmentVariables)
        inMemoryCache[id] = template

        return Ok(template)
    }

    fun setImage(
        id: TemplateName,
        image: String,
    ): Result<Unit, Throwable> = runCatching {
        templateStore.setImage(id, image)
        inMemoryCache[id]?.image = image
    }

    fun addPort(
        id: TemplateName,
        port: Int,
    ) = runCatching {
        templateStore.addPort(id, port)
        inMemoryCache[id]?.ports?.add(port)
        Unit
    }

    fun removePort(
        id: TemplateName,
        port: Int,
    ) = runCatching {
        templateStore.removePort(id, port)
        inMemoryCache[id]?.ports?.remove(port)
        Unit
    }

    fun setEnvironmentVariable(
        id: TemplateName,
        key: String,
        value: String,
    ) = runCatching {
        templateStore.setEnvironmentVariable(id, key, value)
        inMemoryCache[id]?.environmentVariables?.put(key, value)
        Unit
    }

    fun removeEnvironmentVariable(
        id: TemplateName,
        key: String,
    ) = runCatching {
        templateStore.removeEnvironmentVariable(id, key)
        inMemoryCache[id]?.environmentVariables?.remove(key)
        Unit
    }

    fun get(id: TemplateName): ServiceTemplate? = inMemoryCache[id]

    fun exists(
        id: TemplateName,
    ): Boolean {
        if (inMemoryCache.contains(id)) return true

        return templateStore.exists(id)
    }

    fun getAll(): Collection<ServiceTemplate> = inMemoryCache.values
}
