package org.readutf.orchestrator.client.client.listeners

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.hermes.channel.HermesChannel
import org.readutf.hermes.listeners.TypedListener
import org.readutf.orchestrator.client.client.ClientManager
import org.readutf.orchestrator.common.packets.S2CScheduleShutdown

internal class SafeShutdownListener(
    private var clientManager: ClientManager,
) : TypedListener<S2CScheduleShutdown, HermesChannel, Unit> {
    private val logger = KotlinLogging.logger { }

    override fun handle(
        packet: S2CScheduleShutdown,
        channel: HermesChannel,
    ) {
        clientManager.safeShutdownHandler.handleSafeShutdown()

        logger.info { "Received shutdown signal from server" }
    }
}
