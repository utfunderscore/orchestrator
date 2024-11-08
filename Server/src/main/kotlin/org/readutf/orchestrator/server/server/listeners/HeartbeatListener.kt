package org.readutf.orchestrator.server.server.listeners

import org.readutf.hermes.channel.HermesChannel
import org.readutf.orchestrator.server.network.listeners.NoopListener
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.shared.packets.C2SHeartbeatPacket

class HeartbeatListener(
    private val serverManager: ServerManager,
) : NoopListener<C2SHeartbeatPacket> {
    override fun handle(
        packet: C2SHeartbeatPacket,
        channel: HermesChannel,
    ) {
        serverManager.handleHeartbeat(packet.serverHeartbeat)
    }
}
