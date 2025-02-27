package org.readutf.orchestrator.client.client.game

import org.readutf.orchestrator.common.game.Game

public fun interface ActiveGamesProvider {

    public fun getGames(): List<Game>
}
