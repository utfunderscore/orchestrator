package org.readutf.orchestrator.server.network.listeners

import org.readutf.hermes.channel.HermesChannel
import org.readutf.orchestrator.server.game.GameManager
import org.readutf.orchestrator.shared.packets.ServerGamesUpdatePacket

class GamesUpdateListener(
    private val gameManager: GameManager,
) : Listener<ServerGamesUpdatePacket, Unit> {
    override fun handle(
        packet: ServerGamesUpdatePacket,
        channel: HermesChannel,
    ) {
        gameManager.updateGames(packet.serverId, packet.games)
    }
}
