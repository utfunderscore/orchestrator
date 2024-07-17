@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.readutf.orchestrator.client

import org.readutf.orchestrator.client.network.ClientNetworkManager
import org.readutf.orchestrator.shared.kryo.KryoCreator
import org.readutf.orchestrator.shared.server.Server
import org.readutf.orchestrator.shared.server.ServerAddress
import org.readutf.orchestrator.shared.server.ServerHeartbeat
import java.util.*

class ShepardClient(
    serverAddress: ServerAddress,
    supportedGameTypes: List<String>,
) {
    private val networkManager = ClientNetworkManager(KryoCreator.build())
    private val serverId: UUID = UUID.randomUUID()

    init {
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
