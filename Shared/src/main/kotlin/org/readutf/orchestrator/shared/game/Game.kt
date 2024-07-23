package org.readutf.orchestrator.shared.game

import java.util.UUID

data class Game(
    val id: UUID,
    val matchType: String,
    val teams: List<List<UUID>>,
    var gameState: GameState,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Game

        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
