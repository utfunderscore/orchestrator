@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.readutf.orchestrator.client

import org.readutf.orchestrator.client.heartbeat.HeartbeatTask
import org.readutf.orchestrator.client.network.ClientNetworkManager
import org.readutf.orchestrator.shared.kryo.KryoCreator
import org.readutf.orchestrator.shared.server.Server
import org.readutf.orchestrator.shared.server.ServerAddress
import org.readutf.orchestrator.shared.server.ServerHeartbeat
import java.util.*
import java.util.concurrent.Executors

class ShepardClient(
    serverAddress: ServerAddress,
    supportedGameTypes: List<String>,
) {
    private val serverId: UUID = UUID.randomUUID()
    private val networkManager = ClientNetworkManager(KryoCreator.build(), serverId)
    private val scheduledExecutorService = Executors.newScheduledThreadPool(1)

    init {
        scheduledExecutorService.schedule(
            HeartbeatTask(networkManager),
            10,
            java.util.concurrent.TimeUnit.SECONDS,
        )
        networkManager.registerServer(
            Server(
                serverId = serverId,
                address = serverAddress,
                supportedModes = supportedGameTypes,
                heartbeat = ServerHeartbeat(serverId, System.currentTimeMillis()),
            ),
        )
    }

    fun shutdown() {
        networkManager.unregisterServer(serverId)
    }
}
