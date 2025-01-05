package org.readutf.orchestrator.server.container.impl.docker

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.model.HostConfig
import com.github.michaelbull.result.*
import io.javalin.Javalin
import org.readutf.orchestrator.common.utils.SResult
import org.readutf.orchestrator.server.container.ContainerController
import org.readutf.orchestrator.server.container.ContainerTemplate
import org.readutf.orchestrator.server.container.impl.docker.store.DockerTemplateStore
import java.net.InetAddress

class DockerController(
    private val dockerClient: DockerClient,
    private val dockerTemplateStore: DockerTemplateStore,
) : ContainerController<DockerTemplate> {
    private val dockerEndpoints = DockerEndpoints(this, dockerTemplateStore)

    // Server Type -> Pending Containers
    private val pendingContainers = hashMapOf<String, MutableList<String>>()
    private val containerTracker = hashMapOf<String, String>()

    override fun registerEndpoints(javalin: Javalin) {
        javalin.post("/docker/templates/", dockerEndpoints::createTemplate)
        javalin.get("/docker/templates/", dockerEndpoints::listTemplates)
        javalin.get("/docker/templates/{id}", dockerEndpoints::getTemplate)
    }

    override fun create(templateId: String): SResult<String> {
        val containerTemplateResult = getTemplate(templateId)
        if (containerTemplateResult.isErr) return Err(containerTemplateResult.error)
        val containerTemplate = containerTemplateResult.get()!!

        val createCommand = dockerClient.createContainerCmd(containerTemplate.dockerImage)
        if (containerTemplate.hostName != null) {
            createCommand.withHostName(containerTemplate.hostName)
        }
        val hostConfig =
            HostConfig
                .newHostConfig()
                .withBinds(containerTemplate.getBindings())
                .withPortBindings(containerTemplate.getPorts())

        if (containerTemplate.network != null) {
            hostConfig.withNetworkMode(containerTemplate.network)
        }

        return runCatching {
            val createResult = createCommand.exec()

            val id = createResult.id
            containerTracker[id] = containerTemplate.id
            pendingContainers.getOrPut(templateId) { mutableListOf() }.add(id)

            return@runCatching createResult
        }.andThen { createResult ->
            runCatching {
                val containerId = createResult.id
                dockerClient.startContainerCmd(containerId).exec()
                return@runCatching containerId
            }
        }.mapError { it.toString() }
    }

    override fun getAddress(containerId: String): SResult<InetAddress> =
        runCatching {
            val inspect =
                dockerClient
                    .inspectContainerCmd(containerId)
                    .exec()

            val ip = inspect.networkSettings.ipAddress
            InetAddress.getByName(ip)
        }.mapError { it.toString() }

    override fun getContainerTemplate(containerId: String): SResult<ContainerTemplate> =
        containerTracker[containerId]?.let {
            dockerTemplateStore.getTemplate(it)
        } ?: Err("Could not find container")

    override fun getTemplates(): List<DockerTemplate> = dockerTemplateStore.getTemplates()

    @Synchronized
    override fun getPendingContainers(
        templateId: String,
        activeServerIds: Collection<String>,
    ): Collection<String> {
        val pending = pendingContainers.getOrPut(templateId) { mutableListOf() }
        pending.removeIf { activeServerIds.contains(it) }

        return pending
    }

    override fun getTemplate(templateId: String): SResult<DockerTemplate> =
        dockerTemplateStore
            .getTemplate(templateId)
            .toErrorIf(
                predicate = { it == null },
                transform = { "Could not find template" },
            ).map { it!! }
}
