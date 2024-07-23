@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.readutf.orchestrator.client

import org.readutf.orchestrator.client.game.ActiveGameSupplier
import org.readutf.orchestrator.client.game.GameManager
import org.readutf.orchestrator.client.game.GameRequestHandler
import org.readutf.orchestrator.client.network.ClientNetworkManager
import org.readutf.orchestrator.shared.game.GameFinderType
import org.readutf.orchestrator.shared.kryo.KryoCreator
import org.readutf.orchestrator.shared.server.Server
import org.readutf.orchestrator.shared.server.ServerAddress
import org.readutf.orchestrator.shared.server.ServerHeartbeat
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ShepardClient(
    serverAddress: ServerAddress,
    supportedGameTypes: List<String>,
    gameFinderTypes: MutableList<GameFinderType>,
    gameSupplier: ActiveGameSupplier,
    gameRequestHandler: GameRequestHandler,
) {
    private val serverId: UUID = UUID.randomUUID()
    private val networkManager = ClientNetworkManager(KryoCreator.build(), serverId)
    private val scheduledExecutorService = Executors.newScheduledThreadPool(1)
    private val gameManager =
        GameManager(
            networkManager = networkManager,
            gameRequestHandler = gameRequestHandler,
            activeGameSupplier = gameSupplier,
            scheduler = scheduledExecutorService,
        )

    init {
        networkManager.registerServer(
            Server(
                serverId = serverId,
                address = serverAddress,
                gameTypes = supportedGameTypes,
                gameFinders = gameFinderTypes,
                activeGames = gameSupplier.getActiveGames().toMutableList(),
                heartbeat = ServerHeartbeat(serverId, System.currentTimeMillis()),
            ),
        )

        scheduledExecutorService.scheduleAtFixedRate(
            { networkManager.sendHeartbeat() },
            0,
            1,
            TimeUnit.SECONDS,
        )
    }

    fun shutdown() {
        networkManager.shutdown()
        scheduledExecutorService.shutdown()
    }
}
