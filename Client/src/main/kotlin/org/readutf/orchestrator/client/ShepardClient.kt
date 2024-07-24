@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.readutf.orchestrator.client

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
    gameRequestHandler: GameRequestHandler,
) {
    private val serverId: UUID = UUID.randomUUID()
    private val networkManager = ClientNetworkManager(KryoCreator.build(), serverId)
    private val scheduledExecutor = Executors.newScheduledThreadPool(1)

    private val serverManager =
        ServerManager(
            serverId = serverId,
            serverAddress = serverAddress,
            supportedGameTypes = supportedGameTypes,
            gameFinderTypes = gameFinderTypes,
            networkManager = networkManager,
            scheduledExecutor = scheduledExecutor,
        )

    val gameManager =
        GameManager(
            networkManager = networkManager,
            serverManager = serverManager,
            scheduler = scheduledExecutor,
        )

    init {
        serverManager.registerServer()
    }

    fun shutdown() {
        networkManager.shutdown()
        serverManager.shutdown()
        scheduledExecutor.shutdown()
    }
}
