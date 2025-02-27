package org.readutf.orchestrator.client.client

import io.github.oshai.kotlinlogging.KotlinLogging

internal class HeartbeatTask(
    private val clientManager: ClientManager,
) : Runnable {
    private val logger = KotlinLogging.logger {}

    override fun run() {
        logger.debug { "Sending heartbeat" }

        clientManager.sendHeartbeat()
    }
}
