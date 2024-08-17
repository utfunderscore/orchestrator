package org.readutf.orchestrator.shared.game

import java.util.UUID

data class Game(
    val id: UUID,
    val serverId: UUID,
    val matchType: String,
    val teams: List<List<UUID>>,
    var reservation: GameReservation?,
    var gameState: GameState,
) {
    fun isReserved(): Boolean = reservation?.let { it.expiresAt > System.currentTimeMillis() } ?: false

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Game

        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    fun shortId() = id.toString().substring(0, 8)
}
