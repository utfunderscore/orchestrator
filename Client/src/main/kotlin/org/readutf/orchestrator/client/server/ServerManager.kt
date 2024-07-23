package org.readutf.orchestrator.client.server

import org.readutf.orchestrator.client.network.ClientNetworkManager
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class ServerManager(
    networkManager: ClientNetworkManager,
    scheduledExecutor: ScheduledExecutorService,
) {
    init {
        scheduledExecutor.scheduleAtFixedRate(
            { networkManager.sendHeartbeat() },
            0,
            1,
            TimeUnit.SECONDS,
        )
    }
}
