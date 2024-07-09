package org.readutf.orchestrator.shared.packets

import org.readutf.orchestrator.shared.packet.Packet

data class TestPacket(
    val string: String,
    val list: List<String>,
) : Packet
