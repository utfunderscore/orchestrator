package org.readutf.orchestrator.server.server.listeners

import org.readutf.hermes.channel.HermesChannel
import org.readutf.hermes.listeners.TypedListener
import org.readutf.orchestrator.common.packets.C2SUpdateAttribute
import org.readutf.orchestrator.server.Orchestrator
import org.readutf.orchestrator.server.server.ServerManager

class UpdateAttributesListener(val serverManager: ServerManager) : TypedListener<C2SUpdateAttribute, HermesChannel, Unit> {
    override fun handle(
        packet: C2SUpdateAttribute,
        channel: HermesChannel,
    ) {
        serverManager.updateAttribute(
            serverId = packet.serverId,
            key = packet.key,
            jsonNode = Orchestrator.objectMapper.readTree(packet.data),
        )
    }
}
