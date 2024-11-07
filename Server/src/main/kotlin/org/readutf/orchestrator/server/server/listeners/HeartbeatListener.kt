package org.readutf.orchestrator.server.server.listeners

import org.readutf.hermes.channel.HermesChannel
import org.readutf.orchestrator.server.network.listeners.NoopListener
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.shared.packets.C2SServerHeartbeatPacket

class HeartbeatListener(
    private val serverManager: ServerManager,
) : NoopListener<C2SServerHeartbeatPacket> {
    override fun handle(
        packet: C2SServerHeartbeatPacket,
        channel: HermesChannel,
    ) {
        serverManager.handleHeartbeat(packet.serverHeartbeat)
    }
}
