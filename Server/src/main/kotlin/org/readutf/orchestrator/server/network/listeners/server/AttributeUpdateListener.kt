package org.readutf.orchestrator.server.network.listeners.server

import org.readutf.hermes.channel.HermesChannel
import org.readutf.orchestrator.server.network.listeners.NoopListener
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.shared.packets.ServerAttributeUpdate

class AttributeUpdateListener(
    val serverManager: ServerManager,
) : NoopListener<ServerAttributeUpdate> {
    override fun handle(
        packet: ServerAttributeUpdate,
        channel: HermesChannel,
    ) {
        serverManager.setAttribute(packet.serverId, packet.attributeName, packet.attribute)
    }
}
