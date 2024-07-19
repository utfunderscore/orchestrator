package org.readutf.orchestrator.shared.game

import java.util.UUID

data class GameRequestResult(
    val requestId: UUID,
    val serverId: UUID,
    val gameId: UUID,
)
