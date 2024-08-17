package org.readutf.orchestrator.client.network.listeners

import org.readutf.hermes.channel.HermesChannel
import org.readutf.hermes.listeners.TypedListener
import org.readutf.orchestrator.client.game.GameManager
import org.readutf.orchestrator.shared.game.GameReservation
import org.readutf.orchestrator.shared.packets.GameReservePacket
import org.readutf.orchestrator.shared.utils.Result

class GameReserveListener(
    private val gameManager: GameManager,
) : TypedListener<GameReservePacket, HermesChannel, Result<GameReservation>> {
    override fun handle(
        packet: GameReservePacket,
        channel: HermesChannel,
    ): Result<GameReservation> {
        println("Reserve packet received")

        return gameManager.reserveGame(packet.gameId, packet.reservationId)
    }
}
