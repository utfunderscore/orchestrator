package org.readutf.orchestrator.server.server.template

import com.github.dockerjava.api.model.Bind
import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.orchestrator.server.docker.ContainerId
import org.readutf.orchestrator.server.docker.DockerManager
import org.readutf.orchestrator.server.server.ContainerResult
import org.readutf.orchestrator.server.server.RegisteredServer
import org.readutf.orchestrator.server.server.template.store.TemplateStore
import org.readutf.orchestrator.shared.server.Server
import org.readutf.orchestrator.shared.utils.Result
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Manages server templates and creation.
 * @param dockerManager The Docker manager to use for creating servers.
 * @param templateStore The store to use for saving and loading templates.
 */
class ServerTemplateManager(
    private val dockerManager: DockerManager,
    private val templateStore: TemplateStore,
) {
    private val logger = KotlinLogging.logger {}

    /**
     * A list of existing server types.
     */
    private val existingTypes = mutableListOf<ServerTemplate>()

    /**
     * A map of container ids to futures for awaiting servers.
     */
    private val awaitingServers = mutableMapOf<String, CompletableFuture<Result<Server, String>>>()

    private val schedulars = Executors.newSingleThreadScheduledExecutor()

    init {
        existingTypes.addAll(templateStore.loadTemplates())
    }

    internal fun createServer(template: ServerTemplate): ContainerResult<Server> {
        val containerId =
            ContainerId(
                dockerManager
                    .createContainer(
                        imageId = template.dockerImage,
                        hostName = template.hostName,
                        bindings = emptyList(),
                        ports = template.ports.toList(),
                        commands = template.commands.toList(),
                        envVars = template.environmentVariables.toList(),
                        network = template.network,
                    ).onFailure {
                        return ContainerResult.failedPreCreation(it.getError())
                    },
            )

        val future = CompletableFuture<Result<Server, String>>()

        awaitingServers[containerId.shortId] = future

        return ContainerResult.awaitingResult(containerId, future)
    }

    fun handleShutdown(registeredServer: RegisteredServer) {
        logger.info { "Scheduled container deletion for ${registeredServer.serverId}" }
        schedulars.schedule({
            logger.info { "Deleting container ${registeredServer.serverId}" }
            dockerManager.deleteContainer(registeredServer.serverId)
        }, 15, TimeUnit.SECONDS)
    }

    fun registerTemplate(
        templateId: String,
        imageId: String,
    ): Result<Unit, String> {
        if (existingTypes.any { it.templateId.equals(templateId, true) }) {
            return Result.failure("Template with this name already exists.")
        }

        val images =
            dockerManager
                .getExistingDockerImages()
                .onFailure { return Result.failure("Cannot communicate with docker engine.") }

        val image = images.find { it.repoTags.contains(imageId) } ?: return Result.failure("Image does not exist.")

        val template = ServerTemplate(templateId, imageId)

        templateStore.saveTemplate(template)
        existingTypes.add(template)

        return Result.empty()
    }

    fun addPort(
        templateId: String,
        port: String,
    ): Result<Unit, String> {
        val template = getTemplate(templateId) ?: return Result.failure("Could not find template with that id")
        try {
            Bind.parse(port)
        } catch (e: Exception) {
            return Result.failure("Invalid port format.")
        }

        template.ports.add(port)
        templateStore.saveTemplate(template)
        return Result.empty()
    }

    fun removePort(
        id: String,
        port: String,
    ): Result<Unit, String> {
        val template = getTemplate(id) ?: return Result.failure("Could not find template with that id")

        if (template.ports.none { it.equals(port, true) }) {
            return Result.failure("Port not found in template.")
        }

        template.ports.remove(port)
        templateStore.saveTemplate(template)

        return Result.empty()
    }

    fun setHostname(
        templateId: String,
        hostname: String,
    ): Result<Unit, String> {
        val template = getTemplate(templateId) ?: return Result.failure("Could not find template with that id")
        template.hostName = hostname
        templateStore.saveTemplate(template)
        return Result.empty()
    }

    fun addCommand(
        templateId: String,
        command: String,
    ): Result<Unit, String> {
        val template = getTemplate(templateId) ?: return Result.failure("Could not find template with that id")
        template.commands.add(command)
        templateStore.saveTemplate(template)
        return Result.empty()
    }

    fun removeCommand(
        templateId: String,
        command: String,
    ): Result<Unit, String> {
        val template = getTemplate(templateId) ?: return Result.failure("Could not find template with that id")

        if (template.commands.none { it.equals(command, false) }) {
            return Result.failure("Command not found in template.")
        }

        template.commands.remove(command)
        templateStore.saveTemplate(template)
        return Result.empty()
    }

    fun setNetwork(
        templateId: String,
        network: String,
    ): Result<Unit, String> {
        val template = getTemplate(templateId) ?: return Result.failure("Could not find template with that id")
        template.network = network
        templateStore.saveTemplate(template)
        return Result.empty()
    }

    fun addEnvironmentVariable(
        templateId: String,
        env: String,
    ): Result<Unit, String> {
        val template = getTemplate(templateId) ?: return Result.failure("Could not find template with that id")
        template.environmentVariables.add(env)
        templateStore.saveTemplate(template)
        return Result.empty()
    }

    fun removeEnvironmentVariable(
        templateId: String,
        env: String,
    ): Result<Unit, String> {
        val template = getTemplate(templateId) ?: return Result.failure("Could not find template with that id")

        if (template.environmentVariables.none { it.equals(env, false) }) {
            return Result.failure("Environment variable not found in template.")
        }

        template.environmentVariables.remove(env)
        templateStore.saveTemplate(template)
        return Result.empty()
    }

    fun getServerFuture(containerId: String) = awaitingServers[containerId.substring(0, 12)]

    fun getTemplate(templateId: String): ServerTemplate? = existingTypes.find { it.templateId == templateId }

    fun serverTypeExists(name: String): Boolean = existingTypes.any { it.templateId.equals(name, true) }

    fun getTemplates(): List<ServerTemplate> = existingTypes.toList()

    fun removeServerFuture(serverId: String) = awaitingServers.remove(serverId)

    fun setImage(
        id: String,
        image: String,
    ): Result<Unit, String> {
        val template = getTemplate(id) ?: return Result.failure("Could not find template with that id")
        template.dockerImage = image
        templateStore.saveTemplate(template)
        return Result.empty()
    }
}
