package org.readutf.orchestrator.client.network.listeners

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.hermes.channel.HermesChannel
import org.readutf.hermes.listeners.TypedListener
import org.readutf.orchestrator.client.game.GameManager
import org.readutf.orchestrator.shared.game.GameRequestResponse
import org.readutf.orchestrator.shared.packets.GameRequestPacket

class GameRequestListener(
    private val gameManager: GameManager,
) : TypedListener<GameRequestPacket, HermesChannel, GameRequestResponse> {
    private val logger = KotlinLogging.logger { }

    override fun handle(
        packet: GameRequestPacket,
        channel: HermesChannel,
    ): GameRequestResponse {
        val requestHandler = gameManager.gameRequestHandler
        val gameRequest = packet.gameRequest
        if (requestHandler == null) {
            return GameRequestResponse.failure(gameRequest.requestId, "Invalid game request")
        }

        val resultGameId = requestHandler.handleRequest(gameRequest.gameType)

        logger.info { "Game request result = $resultGameId" }

        return resultGameId
            ?.let { GameRequestResponse.success(gameRequest.requestId, gameManager.serverId, resultGameId) }
            ?: GameRequestResponse.failure(gameRequest.requestId, "")
    }
}
