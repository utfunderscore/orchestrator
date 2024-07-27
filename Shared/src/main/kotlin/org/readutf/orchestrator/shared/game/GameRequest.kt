package org.readutf.orchestrator.shared.game

import java.util.UUID

data class GameRequest(
    val requestId: UUID,
    val gameType: String,
)
