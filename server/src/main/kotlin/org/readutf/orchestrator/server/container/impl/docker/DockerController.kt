package org.readutf.orchestrator.server.container.impl.docker

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.model.Container
import com.github.dockerjava.api.model.HostConfig
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.get
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.runCatching
import io.github.oshai.kotlinlogging.KotlinLogging
import io.javalin.Javalin
import org.readutf.orchestrator.common.server.NetworkSettings
import org.readutf.orchestrator.common.utils.LongId
import org.readutf.orchestrator.common.utils.ShortId
import org.readutf.orchestrator.server.container.ContainerController
import org.readutf.orchestrator.server.container.ContainerTemplate
import org.readutf.orchestrator.server.container.impl.docker.store.DockerTemplateStore
import org.readutf.orchestrator.server.utils.UUIDGeneratorV1
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class DockerController(
    private val dockerClient: DockerClient,
    private val dockerTemplateStore: DockerTemplateStore,
) : ContainerController<DockerTemplate> {
    private val logger = KotlinLogging.logger {}

    private val uuidGenerator = UUIDGeneratorV1()
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

    override fun create(templateId: String): Result<String, Throwable> {
        logger.info { "Creating container from template '$templateId'" }
        val containerTemplate = getTemplate(templateId).getOrElse {
            logger.error { " - Could not find template" }
            return Err(it)
        }

        val createCommand = dockerClient.createContainerCmd(containerTemplate.dockerImage)
        if (containerTemplate.hostName != null) {
            createCommand.withHostName(containerTemplate.hostName)
        }

        createCommand.withName("$templateId-${uuidGenerator.generate().toString().replace("-", "")}")

        val hostConfig =
            HostConfig
                .newHostConfig()
                .withBinds(containerTemplate.getDockerBinds())
                .withPortBindings(containerTemplate.getDockerPorts())

        if (containerTemplate.network != null) {
            hostConfig.withNetworkMode(containerTemplate.network)
        }

        createCommand.withHostConfig(hostConfig)

        createCommand.hostConfig?.withAutoRemove(containerTemplate.removeAutomatically)

        return runCatching {
            val createResult = createCommand.exec()

            val id = LongId(createResult.id)
            containerTracker[id.toShortId()] = containerTemplate.id
            pendingContainers.getOrPut(templateId) { HashMap() }[id.toShortId()] = System.currentTimeMillis() + 15_000

            return@runCatching createResult
        }.andThen { createResult ->
            try {
                dockerClient.startContainerCmd(createResult.id).exec()
                Ok(createResult.id)
            } catch (e: Exception) {
                dockerClient.removeContainerCmd(createResult.id).exec()
                Err(e)
            }
        }
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

    override fun getAddress(containerId: ShortId): Result<NetworkSettings, Throwable> = runCatching {
        val inspect =
            dockerClient
                .inspectContainerCmd(containerId.shortId)
                .exec()

        val ports =
            inspect.networkSettings.ports.bindings.values
                .map { bindings -> bindings.map { it.hostPortSpec.toInt() } }
                .flatten()
                .distinct()

        NetworkSettings(
            exposedPorts = ports,
            internalHost = inspect.config.hostName ?: "localhost",
        )
    }

    override fun getContainerTemplate(containerId: ShortId): Result<ContainerTemplate, Throwable> = containerTracker[containerId]?.let {
        dockerTemplateStore.getTemplate(it)
    } ?: Err(Exception("Could not find container with id $containerId"))

    override fun getTemplates(): List<String> = dockerTemplateStore.getAllTemplates(0, 10)

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

    override fun getTemplate(templateId: String): Result<DockerTemplate, Throwable> = dockerTemplateStore
        .getTemplate(templateId)
}
