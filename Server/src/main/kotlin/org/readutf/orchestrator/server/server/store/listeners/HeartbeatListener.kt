package org.readutf.orchestrator.server.server.store.listeners

import org.readutf.hermes.channel.HermesChannel
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.shared.packets.ServerHeartbeatPacket

class HeartbeatListener(
    private val serverManager: ServerManager,
) : Listener<ServerHeartbeatPacket> {
    override fun handle(
        packet: ServerHeartbeatPacket,
        channel: HermesChannel,
    ) {
        serverManager.handleHeartbeat(packet.serverHeartbeat)
    }
}
