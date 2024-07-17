package org.readutf.orchestrator.server.network

import com.esotericsoftware.kryo.kryo5.Kryo
import org.readutf.hermes.PacketManager
import org.readutf.hermes.platform.netty.nettyServer
import org.readutf.hermes.serializer.KryoPacketSerializer
import org.readutf.orchestrator.server.network.listeners.ChannelCloseListener
import org.readutf.orchestrator.server.network.listeners.HeartbeatListener
import org.readutf.orchestrator.server.network.listeners.ServerRegisterListener
import org.readutf.orchestrator.server.network.listeners.ServerUnregisterListener
import org.readutf.orchestrator.server.server.ServerManager

class ServerNetworkManager(
    kryo: Kryo,
    serverManager: ServerManager,
) {
    private var packetManager: PacketManager<*> =
        PacketManager
            .nettyServer(
                hostName = "localhost",
                port = 2980,
                serializer = KryoPacketSerializer(kryo),
            ).editListeners { listeners ->
                listeners.registerListener(ChannelCloseListener(serverManager))
                listeners.registerListener(HeartbeatListener(serverManager))
                listeners.registerListener(ServerRegisterListener(serverManager))
                listeners.registerListener(ServerUnregisterListener(serverManager))
            }.exception { throwable ->
                throwable.printStackTrace()
            }.start()
}
