package safeshutdown

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.proxy.ProxyServer
import org.readutf.orchestrator.client.client.shutdown.SafeShutdownHandler

class SafeShutdown(
    private val proxyServer: ProxyServer,
) : SafeShutdownHandler {
    private var shuttingDown = false

    override fun handleSafeShutdown() {
        if (proxyServer.playerCount == 0) {
            proxyServer.shutdown()
        } else {
            shuttingDown = true
        }
    }

    @Subscribe
    fun onPlayerDisconnect(event: DisconnectEvent) {
        if (shuttingDown && proxyServer.playerCount == 0) {
            proxyServer.shutdown()
        }
    }
}
