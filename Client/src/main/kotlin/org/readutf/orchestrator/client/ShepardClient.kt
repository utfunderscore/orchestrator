@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.readutf.orchestrator.client

import org.readutf.orchestrator.client.game.ActiveGameSupplier
import org.readutf.orchestrator.client.game.GameManager
import org.readutf.orchestrator.client.game.GameRequestHandler
import org.readutf.orchestrator.client.network.ClientNetworkManager
import org.readutf.orchestrator.client.server.ServerManager
import org.readutf.orchestrator.shared.game.GameFinderType
import org.readutf.orchestrator.shared.kryo.KryoCreator
import org.readutf.orchestrator.shared.server.ServerAddress
import java.util.*
import java.util.concurrent.Executors

class ShepardClient(
    serverAddress: ServerAddress,
    supportedGameTypes: List<String>,
    gameFinderTypes: MutableList<GameFinderType>,
    gameSupplier: ActiveGameSupplier,
    gameRequestHandler: GameRequestHandler,
) {
    private val serverId: UUID = UUID.randomUUID()
    private val networkManager = ClientNetworkManager(KryoCreator.build(), serverId)
    private val scheduledExecutor = Executors.newScheduledThreadPool(1)

    val gameManager =
        GameManager(
            networkManager = networkManager,
            gameRequestHandler = gameRequestHandler,
            scheduler = scheduledExecutor,
        )

    private val serverManager =
        ServerManager(
            networkManager = networkManager,
            scheduledExecutor = scheduledExecutor,
        )

    fun shutdown() {
        networkManager.shutdown()
        scheduledExecutor.shutdown()
    }
}
