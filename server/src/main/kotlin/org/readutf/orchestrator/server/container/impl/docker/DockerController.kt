package org.readutf.orchestrator.server.container.impl.docker

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.model.Container
import com.github.dockerjava.api.model.HostConfig
import com.github.michaelbull.result.*
import io.github.oshai.kotlinlogging.KotlinLogging
import io.javalin.Javalin
import org.readutf.orchestrator.common.server.ServerAddress
import org.readutf.orchestrator.common.utils.LongId
import org.readutf.orchestrator.common.utils.SResult
import org.readutf.orchestrator.common.utils.ShortId
import org.readutf.orchestrator.server.container.ContainerController
import org.readutf.orchestrator.server.container.ContainerTemplate
import org.readutf.orchestrator.server.container.impl.docker.store.DockerTemplateStore
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class DockerController(
    private val dockerClient: DockerClient,
    private val dockerTemplateStore: DockerTemplateStore,
) : ContainerController<DockerTemplate> {
    private val logger = KotlinLogging.logger {}

    private val dockerEndpoints = DockerEndpoints(dockerTemplateStore)

    // Server Type -> Pending Containers
    private val pendingContainers = hashMapOf<String, HashMap<ShortId, Long>>()
    private val containerTracker = hashMapOf<ShortId, String>()

    init {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this::cleanup, 0, 1, TimeUnit.SECONDS)
    }

    override fun registerEndpoints(javalin: Javalin) {
        javalin.post("/docker/templates/", dockerEndpoints::createTemplate)
        javalin.get("/docker/templates/", dockerEndpoints::listTemplates)
        javalin.get("/docker/templates/{id}", dockerEndpoints::getTemplate)
    }

    override fun create(templateId: String): SResult<String> {
        logger.info { "Creating container from template '$templateId'" }
        val containerTemplateResult = getTemplate(templateId)
        if (containerTemplateResult.isErr) {
            logger.error { " - Could not find template" }
            return Err(containerTemplateResult.error)
        }
        val containerTemplate = containerTemplateResult.get()!!

        val createCommand = dockerClient.createContainerCmd(containerTemplate.dockerImage)
        if (containerTemplate.hostName != null) {
            createCommand.withHostName(containerTemplate.hostName)
        }
        val hostConfig =
            HostConfig
                .newHostConfig()
                .withBinds(containerTemplate.getDockerBinds())
                .withPortBindings(containerTemplate.getDockerPorts())

        if (containerTemplate.network != null) {
            hostConfig.withNetworkMode(containerTemplate.network)
        }

        createCommand.withHostConfig(hostConfig)

        return runCatching {
            logger.info { createCommand }
            val createResult = createCommand.exec()

            val id = LongId(createResult.id)
            containerTracker[id.toShortId()] = containerTemplate.id
            pendingContainers.getOrPut(templateId) { HashMap() }[id.toShortId()] = System.currentTimeMillis() + 15_000

            return@runCatching createResult
        }.andThen { createResult ->
            runCatching {
                val containerId = createResult.id
                dockerClient.startContainerCmd(containerId).exec()
                return@runCatching containerId
            }
        }.mapError { it.toString() }
    }

    fun cleanup() {
        try {
            val containers = runCatching { dockerClient.listContainersCmd().withShowAll(true).exec() }.getOrElse { emptyList() }

            containers.forEach { container: Container ->
                if (!container.state.equals("running", true)) {
                    pendingContainers.values.forEach {
                        it.remove(ShortId(container.id))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getAddress(containerId: ShortId): SResult<ServerAddress> =
        runCatching {
            val inspect =
                dockerClient
                    .inspectContainerCmd(containerId.shortId)
                    .exec()

            ServerAddress(inspect.networkSettings.ipAddress, 0)
        }.mapError { it.toString() }

    override fun getContainerTemplate(containerId: ShortId): SResult<ContainerTemplate> =
        containerTracker[containerId]?.let {
            dockerTemplateStore.getTemplate(it)
        } ?: Err("Could not find container with id $containerId")

    override fun getTemplates(): List<DockerTemplate> = dockerTemplateStore.getTemplates()

    @Synchronized
    override fun getPendingContainers(
        templateId: String,
        activeServerIds: Collection<ShortId>,
    ): Collection<ShortId> {
        val pending = pendingContainers.getOrPut(templateId) { HashMap() }
        // Remove servers that already exist
        activeServerIds.forEach { pending.remove(it) }

        return pending.filter { it.value > System.currentTimeMillis() }.keys
    }

    override fun getTemplate(templateId: String): SResult<DockerTemplate> =
        dockerTemplateStore
            .getTemplate(templateId)
}
