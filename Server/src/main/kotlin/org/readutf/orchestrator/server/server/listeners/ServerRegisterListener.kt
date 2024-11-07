package org.readutf.orchestrator.server.server.listeners

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.hermes.channel.HermesChannel
import org.readutf.orchestrator.server.network.listeners.Listener
import org.readutf.orchestrator.server.server.RegisteredServer
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.shared.packets.ServerRegisterPacket
import org.readutf.orchestrator.shared.packets.ServerRegisterResponse
import org.readutf.orchestrator.shared.server.Server

class ServerRegisterListener(
    private val serverManager: ServerManager,
) : Listener<ServerRegisterPacket, ServerRegisterResponse> {
    private val logger = KotlinLogging.logger {}

    override fun handle(
        packet: ServerRegisterPacket,
        channel: HermesChannel,
    ): ServerRegisterResponse {
        val server =
            Server(
                serverId = packet.serverId,
                address = packet.address,
                serverType = packet.serverType,
            )

        if (packet.protocolVersion != Server.PROTOCOL_ID) {
            logger.error { "Server ${server.serverId} attempted to register with an unsupported codec version" }
            return ServerRegisterResponse.INVALID_PROTOCOL
        }

        try {
            serverManager
                .registerServer(
                    RegisteredServer.create(
                        server,
                        channel,
                    ),
                ).onFailure {
                    return it.getError()
                }
            return ServerRegisterResponse.SUCCESS
        } catch (e: Exception) {
            e.printStackTrace()
            return ServerRegisterResponse.UNKNOWN_REASON
        }
    }
}
