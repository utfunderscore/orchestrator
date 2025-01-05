package org.readutf.orchestrator.common.packets

import org.readutf.hermes.Packet
import org.readutf.orchestrator.common.server.Heartbeat
import java.util.UUID

class C2SHeartbeatPacket(
    val serverId: UUID,
    val heartbeat: Heartbeat,
) : Packet()
