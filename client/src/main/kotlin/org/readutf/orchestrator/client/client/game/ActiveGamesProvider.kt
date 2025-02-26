package org.readutf.orchestrator.client.client.game

import org.readutf.orchestrator.common.game.Game

fun interface ActiveGamesProvider {

    fun getGames(): List<Game>
}
