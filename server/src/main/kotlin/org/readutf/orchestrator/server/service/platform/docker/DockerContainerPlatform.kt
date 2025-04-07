package org.readutf.orchestrator.server.service.platform.docker

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.PortBinding
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.runCatching
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.annotations.Blocking
import org.jetbrains.exposed.sql.Database
import org.readutf.orchestrator.common.server.ContainerId
import org.readutf.orchestrator.common.server.NetworkSettings
import org.readutf.orchestrator.common.server.ShortContainerId
import org.readutf.orchestrator.common.template.ServiceTemplate
import org.readutf.orchestrator.common.template.TemplateName
import org.readutf.orchestrator.common.utils.runAsync
import org.readutf.orchestrator.server.service.ActiveContainer
import org.readutf.orchestrator.server.service.platform.ContainerPlatform
import java.time.Instant
import kotlin.time.measureTimedValue

class DockerContainerPlatform(
    val dockerClient: DockerClient,
    val database: Database,
) : ContainerPlatform {

    private val logger = KotlinLogging.logger { }
    private val containerHistoryTracker: ContainerTemplateTracker = ContainerTemplateTracker(database)

    val activeContainers = mutableMapOf<ShortContainerId, ActiveContainer>()

    @Synchronized
    @Blocking
    override fun createContainer(serviceTemplate: ServiceTemplate): Result<String, Throwable> {
        val hostConfig = HostConfig.newHostConfig().apply {
            withNetworkMode("orchestrator")
            withPortBindings(serviceTemplate.ports.map { PortBinding.parse(it.toString()) })
            withAutoRemove(true)
        }
        val createContainerCmd = dockerClient.createContainerCmd(serviceTemplate.image)
            .withEnv(serviceTemplate.environmentVariables.map { "${it.key}:${it.value}" })
            .withHostConfig(hostConfig)

        return runCatching {
            val (createResult, duration) = measureTimedValue { createContainerCmd.exec() }
            logger.info { "Created container for ${serviceTemplate.name} in ${duration.inWholeMilliseconds}ms" }
            for (string in createResult.warnings) {
                logger.info { "- WARNING: $string" }
            }
            val id = createResult.id
            val containerId = ContainerId(id).toShort()

            runAsync {
                containerHistoryTracker.storeContainerTemplate(
                    containerId = containerId,
                    templateName = serviceTemplate.name,
                ).onFailure {
                    logger.error(it) { "Failed to store container template" }
                }
            }

            activeContainers[containerId] = ActiveContainer(containerId, serviceTemplate.name, Instant.now())
            return@runCatching createResult.id
        }.andThen { id ->

            runCatching {
                val (startResult, duration) = measureTimedValue { dockerClient.startContainerCmd(id).exec() }

                logger.info { "Started container in ${duration.inWholeMilliseconds}ms" }
                id
            }
        }
    }

    override fun validateImage(imageName: String): Boolean {
        val tags = dockerClient.listImagesCmd().exec().any { it.repoTags.any { it.equals(imageName, true) } }
        return tags
    }

    override fun renewContainer(shortContainerId: ShortContainerId): Result<Unit, Throwable> = runCatching {
        logger.info { "Renewing container with id $shortContainerId" }
        var previousContainerTemplate = containerHistoryTracker.getTemplateName(shortContainerId).getOrElse {
            logger.error(it) { "Could not find template with id $shortContainerId" }
            return Err(it)
        }
        logger.info { "  - Previous container template $previousContainerTemplate exists" }
        activeContainers[shortContainerId] =
            ActiveContainer(
                containerId = shortContainerId,
                templateName = previousContainerTemplate,
                createdAt = Instant.now(),
            )
    }

    override fun getContainers(): List<ActiveContainer> = activeContainers.values.toList()

    override fun getTemplate(shortContainerId: ShortContainerId): TemplateName? = activeContainers[shortContainerId]?.templateName

    override fun getNetworkSettings(shortContainerId: ShortContainerId): NetworkSettings? {
        val containerInfo = dockerClient.inspectContainerCmd(shortContainerId.id).exec()

        val name = containerInfo.config.hostName

//        println(containerInfo.networkSettings.ports.bindings.)

        val ports = containerInfo.networkSettings.ports.bindings.keys.map { it.port }

        return name?.let {
            NetworkSettings(name, ports)
        }
    }
}
