package org.readutf.orchestrator.client.client.game

import com.github.michaelbull.result.getOrThrow
import org.readutf.hermes.channel.HermesChannel
import org.readutf.hermes.listeners.TypedListener
import org.readutf.orchestrator.common.packets.S2CGameRequestPacket
import java.util.UUID

internal class GameRequestListener(
    private val gameRequestHandler: GameRequestHandler,
) : TypedListener<S2CGameRequestPacket, HermesChannel, UUID> {
    override fun handle(packet: S2CGameRequestPacket, channel: HermesChannel): UUID = gameRequestHandler.startGame(
        packet.gameType,
        packet.players,
    ).getOrThrow { it }
}
