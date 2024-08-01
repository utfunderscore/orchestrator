package org.readutf.orchestrator.shared.game

import java.util.UUID

data class GameRequestResult private constructor(
    val requestId: UUID,
    val serverId: UUID?,
    val gameId: UUID?,
    val failureReason: String?,
) {
    fun isSuccess(): Boolean = failureReason == null

    companion object {
        fun success(
            requestId: UUID,
            serverId: UUID,
            gameId: UUID,
        ): GameRequestResult = GameRequestResult(requestId, serverId, gameId, null)

        fun failure(
            requestId: UUID,
            reason: String,
        ): GameRequestResult = GameRequestResult(requestId, null, null, reason)
    }
}
