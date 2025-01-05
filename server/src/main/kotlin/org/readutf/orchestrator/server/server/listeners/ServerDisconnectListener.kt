package org.readutf.orchestrator.server.server.listeners

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.hermes.channel.ChannelClosePacket
import org.readutf.hermes.channel.HermesChannel
import org.readutf.hermes.listeners.TypedListener
import org.readutf.orchestrator.server.server.ServerManager

class ServerDisconnectListener(
    private val serverManager: ServerManager,
) : TypedListener<ChannelClosePacket<*>, HermesChannel, Unit> {
    private val logger = KotlinLogging.logger {}

    override fun handle(
        packet: ChannelClosePacket<*>,
        channel: HermesChannel,
    ) {
        logger.info { "Disconnected.... $channel" }

        val server = serverManager.getServerByChannel(channel) ?: return
        serverManager.unregisteringServer(server.serverId)
    }
}
