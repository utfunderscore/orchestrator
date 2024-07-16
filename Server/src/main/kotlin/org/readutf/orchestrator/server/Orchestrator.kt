package org.readutf.orchestrator.server

import org.readutf.orchestrator.server.network.ServerNetworkManager
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.shared.kryo.KryoCreator

class Orchestrator {
    init {
        val kryo = KryoCreator.build()

        val serverManager = ServerManager()
        val serverNetworkManager = ServerNetworkManager(kryo, serverManager)
    }
}
