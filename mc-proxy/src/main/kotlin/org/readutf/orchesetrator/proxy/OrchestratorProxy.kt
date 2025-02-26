package org.readutf.orchesetrator.proxy

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.ProxyServer
import org.readutf.orchesetrator.proxy.safeshutdown.ProxySafeShutdownHandler
import org.readutf.orchesetrator.proxy.safeshutdown.ProxySafeShutdownListener
import org.readutf.orchestrator.client.ConnectionManager
import org.readutf.orchestrator.client.platform.DockerPlatform

@Plugin(id = "orchestrator", name = "Orchestrator Proxy", authors = ["utfunderscore"])
class OrchestratorProxy
@Inject
constructor(
    private val proxyServer: ProxyServer,
) {
    private val hostAddress = System.getenv("orchestrator.hostaddress") ?: "orchestrator"
    private val maxPlayers = System.getenv("orchestrator.maxplayers")?.toInt() ?: 200
    private val safeShutdownHandler = ProxySafeShutdownHandler(proxyServer)

    init {
        val orchestratorClient = ConnectionManager(
            orchestratorHost = hostAddress,
            platform = DockerPlatform(),
        ).configure {
            safeShutdownHandler(safeShutdownHandler)
            capacityHandler { proxyServer.playerCount.toDouble() / maxPlayers }
        }

        Thread {
            orchestratorClient.connectBlocking()
        }.start()
    }

    @Subscribe
    fun onInitialize(initialized: ProxyInitializeEvent) {
        proxyServer.eventManager.register(this, ProxySafeShutdownListener(proxyServer, safeShutdownHandler))

        proxyServer.eventManager.register(
            this,
            TemporaryDisconnect(),
        )
    }
}
