package org.readutf.orchestrator.server.network.listeners.server

import org.readutf.hermes.channel.HermesChannel
import org.readutf.orchestrator.server.network.listeners.NoopListener
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.shared.packets.ServerAttributeRemove

class AttributeRemoveListener(
    val serverManager: ServerManager,
) : NoopListener<ServerAttributeRemove> {
    override fun handle(
        packet: ServerAttributeRemove,
        channel: HermesChannel,
    ) {
        serverManager.removeAttribute(packet.serverId, packet.attributeName)
    }
}
