package org.readutf.orchestrator.server.server.listeners

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.hermes.channel.HermesChannel
import org.readutf.orchestrator.server.network.listeners.NoopListener
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.shared.packets.ServerAttributeUpdate

class AttributeUpdateListener(
    val serverManager: ServerManager,
) : NoopListener<ServerAttributeUpdate> {
    private val logger = KotlinLogging.logger { }

    override fun handle(
        packet: ServerAttributeUpdate,
        channel: HermesChannel,
    ) {
        logger.info { "Updating attribute ${packet.attributeName} on server ${packet.serverId}" }
        logger.info { "Attribute: ${packet.attribute}" }

        serverManager
            .setAttribute(packet.serverId, packet.attributeName, packet.attribute)
    }
}
