@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.readutf.orchestrator.client

import org.readutf.orchestrator.client.network.ClientNetworkManager
import org.readutf.orchestrator.shared.kryo.KryoCreator
import org.readutf.orchestrator.shared.server.Server
import org.readutf.orchestrator.shared.server.ServerAddress
import java.util.*

class ShepardClient(
    serverAddress: ServerAddress,
) {
    private val networkManager = ClientNetworkManager(KryoCreator.build())

    val supportedGameTypes = mutableListOf<String>()

    private val serverId: UUID = UUID.randomUUID()

    init {
        networkManager.registerServer(
            Server(
                serverId = serverId,
                address = serverAddress,
                supportedModes = supportedGameTypes,
            ),
        )
    }

    fun shutdown() {
        networkManager.unregisterServer(serverId)
    }
}
