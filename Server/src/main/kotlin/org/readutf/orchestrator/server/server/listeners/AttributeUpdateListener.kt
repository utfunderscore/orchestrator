package org.readutf.orchestrator.server.server.listeners

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.hermes.channel.HermesChannel
import org.readutf.orchestrator.server.network.listeners.NoopListener
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.shared.packets.C2SAttributeUpdate

class AttributeUpdateListener(
    val serverManager: ServerManager,
) : NoopListener<C2SAttributeUpdate> {
    private val logger = KotlinLogging.logger { }

    override fun handle(
        packet: C2SAttributeUpdate,
        channel: HermesChannel,
    ) {
        logger.debug { "Updating attribute ${packet.attributeName} on server ${packet.serverId}" }
        logger.debug { "Attribute: ${packet.attribute}" }

        serverManager
            .setAttribute(packet.serverId, packet.attributeName, packet.attribute)
    }
}
