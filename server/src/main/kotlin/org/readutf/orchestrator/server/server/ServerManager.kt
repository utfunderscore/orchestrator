package org.readutf.orchestrator.server.server

import com.fasterxml.jackson.databind.JsonNode
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.runCatching
import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.hermes.channel.HermesChannel
import org.readutf.orchestrator.common.packets.S2CScheduleShutdown
import org.readutf.orchestrator.common.server.Heartbeat
import org.readutf.orchestrator.common.server.Server
import org.readutf.orchestrator.common.server.ShortContainerId
import org.readutf.orchestrator.common.template.TemplateName
import org.readutf.orchestrator.common.utils.DisplayNameGenerator
import org.readutf.orchestrator.server.service.platform.ContainerPlatform
import java.util.UUID

class ServerManager(
    private val containerManager: ContainerPlatform,
) {
    private val logger = KotlinLogging.logger {}
    private val servers = mutableMapOf<UUID, RegisteredServer>()
    private val channelToServer = mutableMapOf<String, UUID>()

    fun registerServer(
        serverId: UUID = UUID.randomUUID(),
        containerId: ShortContainerId,
        channel: HermesChannel,
        attributes: MutableMap<String, JsonNode>,
    ): Result<Server, Throwable> = runCatching {
        logger.info { "Registering server with containerId: $containerId $channel" }
        val networkSettings =
            containerManager.getNetworkSettings(containerId) ?: let {
                logger.info { "Failed to get server address for: $it" }
                error("Failed to get server address for: $containerId")
            }

        val template = containerManager.getTemplate(containerId) ?: run {
            logger.info { "Could not find template for $containerId" }
            error("Could not find template for $containerId")
        }

        val server = Server(
            id = serverId,
            displayName = DisplayNameGenerator.generateDisplayName(),
            shortContainerId = containerId,
            networkSettings = networkSettings,
            templateName = template,
            attributes = attributes.toMutableMap(),
        )

        servers[serverId] = RegisteredServer.fromServer(server, channel)
        channelToServer[channel.channelId] = serverId
        logger.info { "Registered successfully" }

        server
    }

    fun renewServer(
        serverId: UUID,
        containerId: ShortContainerId,
        channel: HermesChannel,
        attributes: MutableMap<String, JsonNode>,
    ) {
        logger.info { "Renewing server with containerId: $containerId $channel" }
        containerManager.renewContainer(containerId)

        registerServer(serverId, containerId, channel, attributes)
    }

    fun unregisteringServer(serverId: UUID) {
        logger.info { "Unregistering server $serverId" }

        val registeredServer = servers.remove(serverId) ?: return
        channelToServer.remove(registeredServer.channel.channelId)
    }

    fun handleHeartbeat(
        serverId: UUID,
        heartbeat: Heartbeat,
    ) {
        val server = servers[serverId]
        if (server == null) {
            logger.info { "Received heartbeat from server that isn't registered" }
            return
        }
        logger.debug { "Heartbeat received for $serverId" }

        server.lastHeartbeat = heartbeat
    }

    fun updateAttribute(serverId: UUID, key: String, jsonNode: JsonNode) {
        val server = servers[serverId]
        if (server == null) {
            logger.info { "Received attribute update from server that isn't registered" }
            return
        }
        logger.debug { "Attribute update received for $key" }

        server.attributes[key] = jsonNode
    }

    fun getServerById(serverId: UUID): RegisteredServer? = servers[serverId]

    fun scheduleShutdown(server: RegisteredServer) {
        server.shuttingDown = true
        server.channel.sendPacket(S2CScheduleShutdown)
        logger.info { "Scheduled shutdown for ${server.id}" }
    }

    fun getServers(): Collection<RegisteredServer> = servers.values

    fun getServersByTemplate(templateName: TemplateName): Collection<RegisteredServer> = servers.values.filter { it.templateName == templateName }

    fun getServerByChannel(channel: HermesChannel): RegisteredServer? {
        val serverId = channelToServer[channel.channelId]

        if (serverId == null) {
            logger.warn { "Could not find channel ${channel.channelId}" }
            return null
        }

        return servers[serverId]
    }
}
