package org.readutf.orchesetrator.proxy.safeshutdown

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.proxy.ProxyServer

class ProxySafeShutdownListener(private val proxyServer: ProxyServer, private val safeShutdownHandler: ProxySafeShutdownHandler) {

    @Subscribe
    fun onPlayerDisconnect(event: DisconnectEvent) {
        if (safeShutdownHandler.shuttingDown && proxyServer.playerCount == 0) {
            proxyServer.shutdown()
        }
    }
}
