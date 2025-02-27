package org.readutf.orchestrator.server.container

import com.github.michaelbull.result.Result
import io.javalin.Javalin
import org.readutf.orchestrator.common.server.NetworkSettings
import org.readutf.orchestrator.common.utils.ShortId

interface ContainerController<T : ContainerTemplate> {
    /**
     * Register any needed api endpoints for managing
     * containers and templates
     */
    fun registerEndpoints(javalin: Javalin)

    /**
     * Create a new container from the given template
     * @param containerTemplate The template to create the container from
     */
    fun create(templateId: String): Result<String, Throwable>

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

    fun getTemplate(templateId: String): Result<T, Throwable>
}
