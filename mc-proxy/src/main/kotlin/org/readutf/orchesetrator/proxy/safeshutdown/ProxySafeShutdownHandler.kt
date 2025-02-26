package org.readutf.orchesetrator.proxy.safeshutdown

import com.velocitypowered.api.proxy.ProxyServer
import org.readutf.orchestrator.client.client.shutdown.SafeShutdownHandler

class ProxySafeShutdownHandler(
    private val proxyServer: ProxyServer,
) : SafeShutdownHandler {
    var shuttingDown = false

    override fun handleSafeShutdown() {
        if (proxyServer.playerCount == 0) {
            proxyServer.shutdown()
        } else {
            shuttingDown = true
        }
    }
}
