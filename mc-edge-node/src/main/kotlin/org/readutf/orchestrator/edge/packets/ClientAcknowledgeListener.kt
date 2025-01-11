package org.readutf.orchestrator.edge.packets

import io.github.oshai.kotlinlogging.KotlinLogging
import net.minestom.server.network.packet.client.login.ClientLoginAcknowledgedPacket
import net.minestom.server.network.packet.server.common.TransferPacket
import org.readutf.orchestrator.edge.finder.TransferFinder
import org.readutf.orchestrator.edge.network.ClientConnection
import org.readutf.orchestrator.edge.network.listener.PacketListener

class ClientAcknowledgeListener(
    private val transferFinder: TransferFinder,
) : PacketListener<ClientLoginAcknowledgedPacket> {
    private val logger = KotlinLogging.logger { }

    override fun onPacket(
        packet: ClientLoginAcknowledgedPacket,
        clientConnection: ClientConnection,
    ) {
        val address = transferFinder.findTransferAddress()
        val username = clientConnection.username ?: "Unknown"

        logger.info { "Transferring $username to ${address.hostname}:${address.port}" }
        clientConnection.networkContext.write(TransferPacket(address.hostname, address.port))
    }
}
