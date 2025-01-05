package org.readutf.orchestrator.client.client

import io.github.oshai.kotlinlogging.KotlinLogging

class HeartbeatTask(
    val clientManager: ClientManager,
) : Runnable {
    private val logger = KotlinLogging.logger {}

    override fun run() {
        logger.info { "Sending heartbeat" }

        clientManager.sendHeartbeat()
    }
}
