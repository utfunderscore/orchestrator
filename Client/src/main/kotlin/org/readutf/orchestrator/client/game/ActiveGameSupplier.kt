package org.readutf.orchestrator.client.game

import org.readutf.orchestrator.shared.game.Game

interface ActiveGameSupplier {
    fun getActiveGames(): List<Game>
}
