package org.readutf.orchestrator.client.client.game

import com.github.michaelbull.result.Result
import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.hermes.channel.HermesChannel
import org.readutf.hermes.listeners.TypedListener
import org.readutf.orchestrator.common.packets.S2CGameRequestPacket
import java.util.UUID

internal class GameRequestListener(
    private val gameRequestHandler: GameRequestHandler,
) : TypedListener<S2CGameRequestPacket, HermesChannel, UUID> {
    private val logger = KotlinLogging.logger { }

    override fun handle(packet: S2CGameRequestPacket, channel: HermesChannel): Result<UUID, Throwable> {
        logger.info { "Received game request for $packet" }

        return gameRequestHandler.startGame(
            packet.gameType,
            packet.players,
        )
    }
}
