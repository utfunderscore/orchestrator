package org.readutf.orchestrator.server

import org.readutf.orchestrator.server.network.ServerNetworkManager
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.server.server.store.impl.MemoryServerStore
import org.readutf.orchestrator.shared.kryo.KryoCreator

class Orchestrator {
    init {
        val kryo = KryoCreator.build()

        val serverStore = MemoryServerStore()
        val serverManager = ServerManager(serverStore)
        val serverNetworkManager = ServerNetworkManager(kryo, serverManager)
    }
}
