package org.readutf.orchestrator.shared.packets

import org.readutf.hermes.Packet
import org.readutf.orchestrator.shared.server.ServerHeartbeat

class C2SHeartbeatPacket(
    val serverHeartbeat: ServerHeartbeat,
) : Packet()
