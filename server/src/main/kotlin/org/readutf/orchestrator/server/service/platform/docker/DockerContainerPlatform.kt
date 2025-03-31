package org.readutf.orchestrator.server.service.platform.docker

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.PortBinding
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.runCatching
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.annotations.Blocking
import org.readutf.orchestrator.common.server.ContainerId
import org.readutf.orchestrator.common.server.NetworkSettings
import org.readutf.orchestrator.common.server.ShortContainerId
import org.readutf.orchestrator.common.template.ServiceTemplate
import org.readutf.orchestrator.server.service.ActiveContainer
import org.readutf.orchestrator.server.service.platform.ContainerPlatform
import java.time.Instant
import kotlin.time.measureTimedValue

class DockerContainerPlatform(val dockerClient: DockerClient) : ContainerPlatform {

    private val logger = KotlinLogging.logger { }

    val activeContainers = mutableMapOf<ShortContainerId, ActiveContainer>()

    @Synchronized
    @Blocking
    override fun createContainer(serviceTemplate: ServiceTemplate): Result<String, Throwable> {
        val hostConfig = HostConfig.newHostConfig().apply {
            withNetworkMode("orchestrator")
            withPortBindings(serviceTemplate.ports.map { PortBinding.parse(it.toString()) })
            withAutoRemove(true)
        }
        val createContainerCmd = dockerClient.createContainerCmd(serviceTemplate.image).withHostConfig(hostConfig)

        return runCatching {
            val (createResult, duration) = measureTimedValue { createContainerCmd.exec() }
            logger.info { "Created container for ${serviceTemplate.name} in ${duration.inWholeMilliseconds}ms" }
            for (string in createResult.warnings) {
                logger.info { "- WARNING: $string" }
            }
            val id = createResult.id
            val containerId = ContainerId(id).toShort()
            activeContainers[containerId] = ActiveContainer(containerId, serviceTemplate, Instant.now())
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

    override fun getContainers(): List<ActiveContainer> = activeContainers.values.toList()

    override fun getTemplate(shortContainerId: ShortContainerId): ServiceTemplate? = activeContainers[shortContainerId]?.template

    override fun getNetworkSettings(shortContainerId: ShortContainerId): NetworkSettings? {
        val containerInfo = dockerClient.inspectContainerCmd(shortContainerId.id).exec()

        val name = containerInfo.config.hostName

        val ports =
            containerInfo.networkSettings.ports.bindings.values
                .map { bindings -> bindings.map { it.hostPortSpec.toInt() } }
                .flatten()
                .distinct()

        return name?.let {
            NetworkSettings(name, ports)
        }
    }
}
