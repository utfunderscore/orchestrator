package org.readutf.orchestrator.shared.game

import org.readutf.orchestrator.shared.server.Server
import java.util.UUID

data class GameRequestResult private constructor(
    val requestId: UUID,
    val server: Server?,
    val gameId: UUID?,
    val failureReason: String?,
) {
    fun isSuccess(): Boolean = failureReason == null

    companion object {
        fun success(
            requestId: UUID,
            server: Server,
            gameId: UUID,
        ): GameRequestResult = GameRequestResult(requestId, server, gameId, null)

        fun failure(
            requestId: UUID,
            reason: String,
        ): GameRequestResult = GameRequestResult(requestId, null, null, reason)
    }
}
