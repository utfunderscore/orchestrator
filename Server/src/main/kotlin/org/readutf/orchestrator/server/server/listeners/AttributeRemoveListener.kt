package org.readutf.orchestrator.server.server.listeners

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.hermes.channel.HermesChannel
import org.readutf.orchestrator.server.network.listeners.NoopListener
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.shared.packets.ServerAttributeRemove

class AttributeRemoveListener(
    val serverManager: ServerManager,
) : NoopListener<ServerAttributeRemove> {
    private val logger = KotlinLogging.logger { }

    override fun handle(
        packet: ServerAttributeRemove,
        channel: HermesChannel,
    ) {
        logger.info { "Removing attribute ${packet.attributeName} from server ${packet.serverId}" }

        serverManager.removeAttribute(packet.serverId, packet.attributeName)
    }
}
