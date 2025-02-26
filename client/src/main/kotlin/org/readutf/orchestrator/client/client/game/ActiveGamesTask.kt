package org.readutf.orchestrator.client.client.game

import org.readutf.orchestrator.client.client.ClientManager

class ActiveGamesTask(
    private val clientManager: ClientManager,
    private val activeGamesProvider: ActiveGamesProvider,
) : Runnable {

    override fun run() {
        clientManager.updateAttribute(
            "activeGames",
            activeGamesProvider.getGames(),
        )
    }
}
