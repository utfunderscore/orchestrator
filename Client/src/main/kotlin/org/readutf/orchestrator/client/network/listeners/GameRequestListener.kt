package org.readutf.orchestrator.client.network.listeners

import org.readutf.hermes.channel.HermesChannel
import org.readutf.hermes.listeners.TypedListener
import org.readutf.orchestrator.client.game.GameManager
import org.readutf.orchestrator.shared.packets.GameRequestPacket

class GameRequestListener(
    private val gameManager: GameManager,
) : TypedListener<GameRequestPacket, HermesChannel> {
    override fun handle(
        packet: GameRequestPacket,
        channel: HermesChannel,
    ) {
//        gameManager.handleGameRequest(packet.gameRequest)
    }
}
