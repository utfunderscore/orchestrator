package org.readutf.orchestrator.client.game

import org.readutf.orchestrator.client.network.ClientNetworkManager
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class GameManager(
    private val networkManager: ClientNetworkManager,
    private val gameRequestHandler: GameRequestHandler,
    private val activeGameSupplier: ActiveGameSupplier,
    scheduler: ScheduledExecutorService,
) {
    init {
        scheduler.scheduleAtFixedRate(
            { networkManager.updateGames(activeGameSupplier.getActiveGames()) },
            0,
            5,
            TimeUnit.SECONDS,
        )
    }

//    fun handleGameRequest(gameRequest: GameRequest) {
//
//    }
}
