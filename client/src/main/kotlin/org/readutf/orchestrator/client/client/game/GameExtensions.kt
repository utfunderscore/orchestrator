package org.readutf.orchestrator.client.client.game

import org.readutf.orchestrator.client.client.ClientManager
import org.readutf.orchestrator.common.game.GameFinderType
import org.readutf.orchestrator.common.game.GameServerSettings
import java.util.concurrent.TimeUnit

public fun ClientManager.enableGameServer(
    supportedGames: List<String>,
    finderTypes: List<GameFinderType>,
    activeGamesProvider: ActiveGamesProvider,
    gameRequestHandler: GameRequestHandler,
) {
    taskExecutor.scheduleAtFixedRate(ActiveGamesTask(this, activeGamesProvider), 0, 1, TimeUnit.SECONDS)
    updateAttribute("gameSettings", GameServerSettings(supportedGames, finderTypes))
    packetManager.editListeners {
        it.registerListener(GameRequestListener(gameRequestHandler))
    }
}
