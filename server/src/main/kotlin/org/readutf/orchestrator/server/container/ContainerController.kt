package org.readutf.orchestrator.server.container

import io.javalin.Javalin
import org.readutf.orchestrator.common.utils.SResult
import org.readutf.orchestrator.common.utils.ShortId
import java.net.InetAddress
import java.util.concurrent.CompletableFuture

typealias ContainerResult<T> = SResult<CompletableFuture<SResult<T>>>

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
    fun create(templateId: String): SResult<String>

    /**
     * Get the address of the container
     * @param containerId The id of the container
     */
    fun getAddress(containerId: ShortId): SResult<InetAddress>

    fun getContainerTemplate(containerId: ShortId): SResult<ContainerTemplate>

    /**
     * List all existing templates
     * @return List of templates
     */
    fun getTemplates(): List<T>

    /**
     * When a container is created, it may take time to be registered as an active server,
     * this method returns a list of containers that are pending registration
     */
    fun getPendingContainers(
        templateId: String,
        activeServerIds: Collection<ShortId>,
    ): Collection<ShortId>

    fun getTemplate(templateId: String): SResult<T>
}
