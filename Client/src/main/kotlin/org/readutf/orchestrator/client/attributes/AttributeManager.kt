package org.readutf.orchestrator.client.attributes

import org.readutf.orchestrator.client.ClientManager
import org.readutf.orchestrator.shared.packets.C2SAttributeUpdate

class AttributeManager(
    val serverId: String,
) {
    private val attributes = mutableMapOf<String, Any>()

    fun setAttribute(
        key: String,
        value: Any,
    ) {
        attributes[key] = value
    }

    fun onConnect(clientManager: ClientManager) {
        attributes.forEach { (key, value) ->
            clientManager.packetManager.sendPacket(C2SAttributeUpdate(serverId, key, value))
        }
    }
}
