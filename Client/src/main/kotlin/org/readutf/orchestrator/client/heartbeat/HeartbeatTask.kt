package org.readutf.orchestrator.client.heartbeat

import org.readutf.orchestrator.client.network.ClientNetworkManager

class HeartbeatTask(
    private val networkManager: ClientNetworkManager,
) : Runnable {
    override fun run() {
        networkManager.sendHeartbeat()
    }
}
