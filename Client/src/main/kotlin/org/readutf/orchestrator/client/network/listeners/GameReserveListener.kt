package org.readutf.orchestrator.client.network.listeners

import org.readutf.hermes.channel.HermesChannel
import org.readutf.hermes.listeners.TypedListener
import org.readutf.orchestrator.client.game.GameManager
import org.readutf.orchestrator.shared.packets.GameReservePacket

class GameReserveListener(
    private val gameManager: GameManager,
) : TypedListener<GameReservePacket, HermesChannel> {
    override fun handle(
        packet: GameReservePacket,
        channel: HermesChannel,
    ): Boolean = gameManager.reserveGame(packet.gameId)
}
