package org.readutf.orchestrator.common.packets

import org.readutf.hermes.Packet

class S2CGameReservePacket(
    val gameId: String,
) : Packet<Boolean>()
