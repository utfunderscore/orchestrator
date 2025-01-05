package org.readutf.orchestrator.common.packets

import org.readutf.hermes.Packet

data class C2SRegisterPacket(
    val containerId: String,
) : Packet()
