package org.readutf.orchestrator.server.network.listeners

import org.readutf.hermes.channel.HermesChannel
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.shared.packets.ServerGamesUpdatePacket

class GamesUpdateListener(
    private val serverManager: ServerManager,
) : Listener<ServerGamesUpdatePacket, Unit> {
    override fun handle(
        packet: ServerGamesUpdatePacket,
        channel: HermesChannel,
    ) {
        serverManager.updateGames(packet.serverId, packet.games)
    }
}
