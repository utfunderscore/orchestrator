package org.readutf.orchestrator.edge.packets

import com.google.gson.Gson
import net.minestom.server.MinecraftServer
import net.minestom.server.network.packet.client.status.StatusRequestPacket
import net.minestom.server.network.packet.server.status.ResponsePacket
import org.readutf.orchestrator.edge.network.ClientConnection
import org.readutf.orchestrator.edge.network.listener.PacketListener
import org.readutf.orchestrator.edge.status.Description
import org.readutf.orchestrator.edge.status.Players
import org.readutf.orchestrator.edge.status.StatusRequestResponse
import org.readutf.orchestrator.edge.status.Version

class ServerStatusListener : PacketListener<StatusRequestPacket> {
    private val status: String =
        Gson().toJson(
            StatusRequestResponse(
                version =
                    Version(
                        name = MinecraftServer.VERSION_NAME,
                        protocol = MinecraftServer.PROTOCOL_VERSION,
                    ),
                players = Players(max = 0, online = 0),
                description = Description("Proxy Balancer v1.0.5"),
                enforcesSecureChat = false,
                previewsChat = false,
            ),
        )

    override fun onPacket(
        packet: StatusRequestPacket,
        clientConnection: ClientConnection,
    ) {
        clientConnection.networkContext.write(ResponsePacket(status))
    }
}
