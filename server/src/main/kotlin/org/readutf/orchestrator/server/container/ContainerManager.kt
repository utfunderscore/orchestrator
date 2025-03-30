package org.readutf.orchestrator.server.container

import com.fasterxml.jackson.databind.JsonNode
import com.github.michaelbull.result.Result
import org.readutf.orchestrator.common.server.NetworkSettings
import org.readutf.orchestrator.common.template.ContainerTemplate
import org.readutf.orchestrator.common.utils.ShortId

interface ContainerManager<T : ContainerTemplate> {

    /**
     * Create a new container from the given template
     * @param templateId The template to create the container from
     */
    fun createContainer(templateId: String): Result<String, Throwable>

    fun createTemplate(templateId: String, jsonNode: JsonNode): Result<T, Throwable>

    fun deleteTemplate(templateId: String): Result<Unit, Throwable>

    /**
     * Get the address of the container
     * @param containerId The id of the container
     */
    fun getAddress(containerId: ShortId): Result<NetworkSettings, Throwable>

    fun getContainerTemplate(containerId: ShortId): Result<ContainerTemplate, Throwable>

    /**
     * List all existing templates
     * @return List of templates
     */
    fun getTemplates(): List<String>

    /**
     * When a container is created, it may take time to be registered as an active server,
     * this method returns a list of containers that are pending registration
     */
    fun getPendingContainers(
        templateId: String,
        activeServerIds: Collection<ShortId>,
    ): Collection<ShortId>

    fun getTemplates(templateId: String): Result<T, Throwable>
}
