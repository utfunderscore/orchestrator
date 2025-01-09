package org.readutf.orchestrator.client.client.shutdown

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.hermes.channel.HermesChannel
import org.readutf.hermes.listeners.TypedListener
import org.readutf.orchestrator.common.packets.S2CScheduleShutdown

class SafeShutdownListener(
    private var safeShutdownHandler: SafeShutdownHandler,
) : TypedListener<S2CScheduleShutdown, HermesChannel, Unit> {
    private val logger = KotlinLogging.logger { }

    override fun handle(
        packet: S2CScheduleShutdown,
        channel: HermesChannel,
    ) {
        safeShutdownHandler.handleSafeShutdown()

        logger.info { "Received shutdown signal from server" }
    }
}
