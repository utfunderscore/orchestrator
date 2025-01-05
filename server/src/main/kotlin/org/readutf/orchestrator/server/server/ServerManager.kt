package org.readutf.orchestrator.server.server

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.andThen
import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.hermes.channel.HermesChannel
import org.readutf.orchestrator.common.packets.S2CScheduleShutdown
import org.readutf.orchestrator.common.server.Heartbeat
import org.readutf.orchestrator.common.server.Server
import org.readutf.orchestrator.common.utils.DisplayNameGenerator
import org.readutf.orchestrator.common.utils.SResult
import org.readutf.orchestrator.server.container.ContainerController
import java.util.UUID

class ServerManager(
    private val containerController: ContainerController<*>,
) {
    private val logger = KotlinLogging.logger {}
    private val servers = mutableMapOf<UUID, RegisteredServer>()
    private val channelToServer = mutableMapOf<String, UUID>()

    fun registerServer(
        containerId: String,
        channel: HermesChannel,
    ): SResult<Server> {
        logger.info { "Registering server with containerId: $containerId $channel" }

        val serverId = UUID.randomUUID()

        val server = Server(serverId, DisplayNameGenerator.generateDisplayName(), containerId)

        return containerController
            .getContainerTemplate(containerId)
            .andThen { container ->
                servers[serverId] = RegisteredServer.fromServer(server, channel, container)
                channelToServer[channel.channelId] = serverId
                Ok(server)
            }
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

    fun getServerById(serverId: UUID): RegisteredServer? = servers[serverId]

    fun scheduleShutdown(server: RegisteredServer) {
        server.shuttingDown = true
        server.channel.sendPacket(S2CScheduleShutdown)
    }

    fun getServers(): Collection<RegisteredServer> = servers.values

    /**
     * Get all servers created from a template that is not shuttingDown
     */
    fun getActiveServersByTemplate(templateId: String): Collection<RegisteredServer> =
        servers.values
            .filter {
                it.template.templateId.equals(templateId, true)
            }.filter { !it.shuttingDown }

    fun getServerByChannel(channel: HermesChannel): RegisteredServer? {
        val serverId = channelToServer[channel.channelId]

        if (serverId == null) {
            logger.warn { "Could not find channel ${channel.channelId}" }
            return null
        }

        return servers[serverId]
    }
}
