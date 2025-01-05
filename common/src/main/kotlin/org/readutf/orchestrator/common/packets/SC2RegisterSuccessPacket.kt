package org.readutf.orchestrator.common.packets

import org.readutf.hermes.Packet
import org.readutf.orchestrator.common.server.Server

data class SC2RegisterSuccessPacket(
    val server: Server,
) : Packet()
