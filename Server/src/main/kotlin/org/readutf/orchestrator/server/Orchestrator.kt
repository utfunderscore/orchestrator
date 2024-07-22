package org.readutf.orchestrator.server

import io.javalin.Javalin
import org.readutf.orchestrator.server.game.GameManager
import org.readutf.orchestrator.server.network.ServerNetworkManager
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.server.server.store.impl.MemoryServerStore
import org.readutf.orchestrator.shared.kryo.KryoCreator

class Orchestrator {
    init {
        val kryo = KryoCreator.build()

        val javalin = setupJavalin()
        val serverStore = MemoryServerStore()
        val serverManager = ServerManager(serverStore)
        val gameManager = GameManager(javalin, serverManager)
        val serverNetworkManager = ServerNetworkManager(kryo, serverManager)
    }

    fun setupJavalin() =
        Javalin.createAndStart {
            it.jetty.defaultHost = "localhost"
            it.jetty.defaultPort = 9393
        }
}
