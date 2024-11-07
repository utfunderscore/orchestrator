package org.readutf.orchestrator.shared.game

import org.readutf.orchestrator.shared.server.Server
import java.util.UUID

data class GameRequestResponse private constructor(
    val requestId: UUID,
    val serverId: String?,
    val gameId: UUID?,
    val failureReason: String?,
) {
    fun isSuccess(): Boolean = failureReason == null

    fun toResult(idToServer: (String) -> Server?): GameRequestResult =
        if (isSuccess()) {
            val foundServer = idToServer(serverId!!)
            if (foundServer == null) {
                GameRequestResult.failure(requestId, "Could not find server for game")
            } else {
                GameRequestResult.success(requestId, foundServer, gameId!!)
            }
        } else {
            GameRequestResult.failure(requestId, failureReason!!)
        }

    companion object {
        fun success(
            requestId: UUID,
            serverId: String,
            gameId: UUID,
        ): GameRequestResponse = GameRequestResponse(requestId, serverId, gameId, null)

        fun failure(
            requestId: UUID,
            reason: String,
        ): GameRequestResponse = GameRequestResponse(requestId, null, null, reason)
    }
}
