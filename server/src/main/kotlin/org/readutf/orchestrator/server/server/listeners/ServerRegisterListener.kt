package org.readutf.orchestrator.server.server.listeners

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.github.michaelbull.result.onFailure
import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.hermes.channel.HermesChannel
import org.readutf.hermes.listeners.TypedListener
import org.readutf.orchestrator.common.packets.C2SRegisterPacket
import org.readutf.orchestrator.common.server.ShortContainerId
import org.readutf.orchestrator.server.server.ServerManager
import java.util.UUID

class ServerRegisterListener(
    private val serverManager: ServerManager,
) : TypedListener<C2SRegisterPacket, HermesChannel, UUID> {
    private val logger = KotlinLogging.logger { }

    override fun handle(
        packet: C2SRegisterPacket,
        channel: HermesChannel,
    ): Result<UUID, Throwable> = serverManager.registerServer(
        containerId = ShortContainerId.of(packet.containerId),
        channel = channel,
        attributes = packet.attributes.toMutableMap(),
    ).map { it.id }.onFailure { logger.error(it) {} }
}
