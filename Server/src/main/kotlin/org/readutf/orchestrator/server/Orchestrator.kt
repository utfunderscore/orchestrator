package org.readutf.orchestrator.server

import com.esotericsoftware.kryo.kryo5.Kryo
import io.javalin.Javalin
import org.readutf.hermes.PacketManager
import org.readutf.hermes.platform.netty.nettyClient
import org.readutf.hermes.serializer.KryoPacketSerializer
import org.readutf.orchestrator.server.game.GameManager
import org.readutf.orchestrator.server.network.ServerNetworkManager
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.server.server.store.impl.MemoryDataStore
import org.readutf.orchestrator.shared.kryo.KryoCreator

class Orchestrator {
    init {
        val kryo = KryoCreator.build()
        val packetManager = setupPacketManager(kryo)
        val javalin = setupJavalin()
        val serverStore = MemoryDataStore()
        val serverManager = ServerManager(serverStore)
        val gameManager = GameManager(javalin, serverManager, packetManager)
        val serverNetworkManager = ServerNetworkManager(kryo, serverManager)
    }

    private fun setupJavalin() =
        Javalin.createAndStart {
            it.jetty.defaultHost = "localhost"
            it.jetty.defaultPort = 9393
        }

    private fun setupPacketManager(kryo: Kryo) =
        PacketManager
            .nettyClient(
                hostName = "localhost",
                port = 2980,
                serializer = KryoPacketSerializer(kryo),
            ).start()
}
