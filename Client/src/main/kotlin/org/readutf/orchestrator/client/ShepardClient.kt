@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.readutf.orchestrator.client

import org.readutf.orchestrator.client.network.ClientNetworkManager
import org.readutf.orchestrator.shared.game.Game
import org.readutf.orchestrator.shared.kryo.KryoCreator
import org.readutf.orchestrator.shared.server.Server
import org.readutf.orchestrator.shared.server.ServerAddress
import org.readutf.orchestrator.shared.server.ServerHeartbeat
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.SECONDS

class ShepardClient(
    serverAddress: ServerAddress,
    supportedGameTypes: List<String>,
    private val gameSupplier: () -> List<Game>,
) {
    private val serverId: UUID = UUID.randomUUID()
    private val networkManager = ClientNetworkManager(KryoCreator.build(), serverId)
    private val scheduledExecutorService = Executors.newScheduledThreadPool(1)
    private val previousServer = mutableListOf<Game>()

    init {
        networkManager.registerServer(
            Server(
                serverId = serverId,
                address = serverAddress,
                supportedModes = supportedGameTypes,
                activeGames = gameSupplier.invoke().toMutableList(),
                heartbeat = ServerHeartbeat(serverId, System.currentTimeMillis()),
            ),
        )
        scheduledExecutorService.scheduleAtFixedRate(
            { networkManager.sendHeartbeat() },
            0,
            1,
            SECONDS,
        )
        scheduledExecutorService.scheduleAtFixedRate(
            {
                val activeGames = gameSupplier.invoke()
                if (activeGames != previousServer) {
                    networkManager.updateGames(activeGames)
                }
            },
            0,
            5,
            SECONDS,
        )
    }

    fun shutdown() {
        networkManager.shutdown()
        scheduledExecutorService.shutdown()
    }
}
