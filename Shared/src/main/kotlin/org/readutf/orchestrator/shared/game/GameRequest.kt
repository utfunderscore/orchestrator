package org.readutf.orchestrator.shared.game

data class GameRequest(
    val queueName: String,
    val numberOfTeams: Int,
    val teamSize: Int,
)
