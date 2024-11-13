package org.readutf.orchestrator.server.docker

import com.github.dockerjava.api.command.StartContainerCmd
import com.github.dockerjava.api.model.Bind
import com.github.dockerjava.api.model.Container
import com.github.dockerjava.api.model.Image
import com.github.dockerjava.api.model.PortBinding
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient
import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.orchestrator.server.settings.DockerSettings
import org.readutf.orchestrator.shared.utils.Result
import org.readutf.orchestrator.shared.utils.catch
import java.net.URI
import java.time.Duration

/**
 * Manages the Docker client and containers.
 * @param dockerSettings The settings for the Docker client.
 */
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

    /**
     * Gets a container by its short id.
     * @param shortId The short id of the container to get.
     * @return A [Result] containing the container if successful, or an error message if not.
     * @see Result
     */
    fun getContainerByShortId(shortId: String): Result<Container, String> {
        val containers =
            dockerClient
                .listContainersCmd()
                .withShowAll(true)
                .exec()

        val found = containers.filter { container -> container.id.startsWith(shortId) }

        if (found.isEmpty()) {
            return Result.failure("Could not find container with id $shortId")
        }

        return Result.success(found.first())
    }

    fun getContainersByImageId(imageId: String): Result<List<Container>, String> {
        val containers =
            dockerClient
                .listContainersCmd()
                .withShowAll(true)
                .exec()

        println("images: " + containers.map { it.imageId }.joinToString())

        val found = containers.filter { container -> container.image.startsWith(imageId) }

        if (found.isEmpty()) {
            return Result.failure("Could not find container with image id $imageId")
        }

        return Result.success(found)
    }

    /**
     * Creates a container with the specified image id, hostname, network, bindings, ports, env vars, and commands.
     * @param imageId The id of the image to create the container from.
     * @param hostName The hostname of the container.
     * @param network The network to connect the container to.
     * @param bindings The bindings to apply to the container.
     * @param ports The ports to bind to the container.
     * @param envVars The environment variables to set in the container.
     * @param commands The commands to run in the container.
     * @return A [Result] containing the id of the created container if successful, or an error message if not.
     * @see Result
     */
    fun createContainer(
        imageId: String,
        hostName: String?,
        network: String?,
        bindings: List<String>,
        ports: List<String>,
        envVars: List<String>,
        commands: List<String>,
    ): Result<String, String> {
        var createCommand =
            dockerClient
                .createContainerCmd(imageId)

        if (hostName != null) createCommand = createCommand.withHostName(hostName)

        catch {
            bindings.forEach { createCommand.withBinds(Bind.parse(it)) }
        }.mapError { return it }

        catch {
            ports.forEach { createCommand.withPortBindings(PortBinding.parse(it)) }
        }.mapError { return it }

        catch {
            envVars.forEach { createCommand.withEnv(it) }
        }.mapError { return it }

        catch {
            commands.forEach { createCommand.withCmd(it) }
        }.mapError { return it }

        catch {
            createCommand.withNetworkMode(network)
        }.mapError { return it }

        val containerId = createCommand.exec()

        logger.debug { "Created container with id ${containerId.id}..." }

        val startCommand: StartContainerCmd = dockerClient.startContainerCmd(containerId.id)

        startCommand.exec()

        logger.debug { "Started container with id $containerId..." }

        return Result.success(containerId.id)
    }

    fun stopContainer(containerShortId: String): Result<Unit, String> {
        catch {
            dockerClient.stopContainerCmd(containerShortId).exec()
        }

        logger.debug { "Stopped container with id $containerShortId..." }

        return Result.success(Unit)
    }

    fun deleteContainer(
        containerShortId: String,
        force: Boolean = true,
    ): Result<Unit, String> {
        catch {
            dockerClient.removeContainerCmd(containerShortId).withForce(force).exec()
        }.mapError { return it }

        logger.debug { "Stopped and removed container with id $containerShortId..." }

        return Result.success(Unit)
    }

    /**
     * Removes a container by its id.
     * @param containerId The id of the container to remove.
     * @return A [Result] containing the id of the removed container if successful, or an error message if not.
     * @see Result
     */
    fun getExistingDockerImages(): Result<List<Image>, String> {
        try {
            val images =
                dockerClient
                    .listImagesCmd()
                    .withShowAll(true)
                    .exec()
            return Result.success(images)
        } catch (e: Exception) {
            logger.error(e) { "Could not get images from docker" }
            return Result.failure("Could not get images from docker")
        }
    }
}
