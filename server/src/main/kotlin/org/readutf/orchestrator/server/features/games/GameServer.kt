package org.readutf.orchestrator.server.features.games

import org.readutf.orchestrator.common.game.Game
import org.readutf.orchestrator.common.server.Server

data class GameServer(val server: Server, val game: Game)
