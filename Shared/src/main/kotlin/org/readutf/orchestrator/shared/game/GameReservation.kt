package org.readutf.orchestrator.shared.game

import java.util.UUID

data class GameReservation(
    val id: UUID,
    val reservedAt: Long,
    val expiresAt: Long,
)
