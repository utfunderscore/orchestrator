package org.readutf.orchestrator.server.server

import com.fasterxml.jackson.databind.JsonNode
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.hermes.channel.HermesChannel
import org.readutf.orchestrator.common.packets.S2CScheduleShutdown
import org.readutf.orchestrator.common.server.Heartbeat
import org.readutf.orchestrator.common.server.Server
import org.readutf.orchestrator.common.server.ShortContainerId
import org.readutf.orchestrator.common.utils.DisplayNameGenerator
import org.readutf.orchestrator.server.service.platform.ContainerPlatform
import java.util.UUID

class ServerManager(
    private val containerManager: ContainerPlatform,
) {
    private val logger = KotlinLogging.logger {}
    private val servers = mutableMapOf<UUID, RegisteredServer>()
    private val channelToServer = mutableMapOf<String, UUID>()
    private val serverCreateListeners = mutableListOf<(Server) -> Unit>()

    fun registerServer(
        containerId: String,
        channel: HermesChannel,
        attributes: Map<String, JsonNode>,
    ): Result<Server, Throwable> {
        logger.info { "Registering server with containerId: $containerId $channel" }

        val serverId = UUID.randomUUID()
        val shortId = ShortContainerId.of(containerId)

        return Err(Exception("test"))

        val networkSettings =
            containerManager.getNetworkSettings(shortId) ?: let {
                logger.info { "Failed to get server address for: $it" }
                return Err(Throwable("Server address not found"))
            }

        val server = Server(serverId, DisplayNameGenerator.generateDisplayName(), shortId, networkSettings, mutableMapOf())
        servers[serverId] = RegisteredServer.fromServer(server, channel)
        channelToServer[channel.channelId] = serverId
        return Ok(server)
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

    fun getServerByChannel(channel: HermesChannel): RegisteredServer? {
        val serverId = channelToServer[channel.channelId]

        if (serverId == null) {
            logger.warn { "Could not find channel ${channel.channelId}" }
            return null
        }

        return servers[serverId]
    }
}
