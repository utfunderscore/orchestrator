package org.readutf.orchestrator.server.game

data class GameRequest(
    val queueName: String,
    val numberOfTeams: Int,
    val teamSize: Int,
)
