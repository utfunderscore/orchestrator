package org.readutf.orchestrator.server.network

import com.esotericsoftware.kryo.kryo5.Kryo
import org.readutf.hermes.PacketManager
import org.readutf.hermes.platform.netty.nettyServer
import org.readutf.hermes.serializer.KryoPacketSerializer
import org.readutf.orchestrator.server.server.ServerListeners
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
                listeners.registerAll(ServerListeners(serverManager))
            }.exception { throwable ->
                throwable.printStackTrace()
            }.start()
}
