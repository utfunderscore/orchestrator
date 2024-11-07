package org.readutf.orchestrator.shared.packets

import org.readutf.hermes.Packet

data class ServerAttributeRemove(
    val serverId: String,
    val attributeName: String,
) : Packet()
