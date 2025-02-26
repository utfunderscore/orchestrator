package org.readutf.orchestrator.common.game

data class GameServerSettings(
    val supportedGames: List<String>,
    val finderTypes: List<GameFinderType>,
)
