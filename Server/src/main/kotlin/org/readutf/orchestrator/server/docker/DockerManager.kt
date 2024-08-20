package org.readutf.orchestrator.server.docker

import com.github.dockerjava.api.model.Container
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient
import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.orchestrator.server.settings.DockerSettings
import org.readutf.orchestrator.shared.utils.Result
import java.net.URI
import java.time.Duration

class DockerManager(
    dockerSettings: DockerSettings,
) {
    private val logger = KotlinLogging.logger { }

    private var config: DockerClientConfig =
        DefaultDockerClientConfig
            .createDefaultConfigBuilder()
            .build()

    private val httpClient =
        ZerodepDockerHttpClient
            .Builder()
            .dockerHost(URI(dockerSettings.uri))
            .maxConnections(dockerSettings.maxConnections)
            .connectionTimeout(Duration.ofSeconds(dockerSettings.connectionTimeout))
            .responseTimeout(Duration.ofSeconds(dockerSettings.responseTimeout))
            .build()

    private val dockerClient = DockerClientImpl.getInstance(config, httpClient)

    fun getContainerByShortId(shortId: String): Result<Container> {
        val containers = dockerClient.listContainersCmd().exec()

        val found = containers.filter { container -> container.id.startsWith(shortId) }

        if (found.isEmpty()) {
            return Result.error("Could not find container with id $shortId")
        }

        return Result.ok(found.first())
    }
}
