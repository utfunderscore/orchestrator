package org.readutf.orchestrator.server

import com.esotericsoftware.kryo.kryo5.Kryo
import io.github.oshai.kotlinlogging.KotlinLogging
import io.javalin.Javalin
import org.readutf.hermes.PacketManager
import org.readutf.hermes.platform.netty.nettyServer
import org.readutf.hermes.serializer.KryoPacketSerializer
import org.readutf.orchestrator.server.game.GameManager
import org.readutf.orchestrator.server.network.exception.SocketExceptionHandler
import org.readutf.orchestrator.server.network.listeners.ChannelCloseListener
import org.readutf.orchestrator.server.network.listeners.GamesUpdateListener
import org.readutf.orchestrator.server.network.listeners.HeartbeatListener
import org.readutf.orchestrator.server.network.listeners.ServerRegisterListener
import org.readutf.orchestrator.server.network.listeners.ServerUnregisterListener
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.server.server.store.impl.MemoryDataStore
import org.readutf.orchestrator.shared.kryo.KryoCreator
import java.net.SocketException

class Orchestrator {
    private val logger = KotlinLogging.logger { }

    init {
        val kryo = KryoCreator.build()
        val javalin = setupJavalin()
        val serverStore = MemoryDataStore()
        val serverManager = ServerManager(serverStore)
        val packetManager = setupPacketManager(serverManager, kryo)
        val gameManager = GameManager(javalin, serverManager, packetManager)
    }

    private fun setupJavalin() =
        Javalin.createAndStart {
            it.jetty.defaultHost = "localhost"
            it.jetty.defaultPort = 9393
        }

    fun setupPacketManager(
        serverManager: ServerManager,
        kryo: Kryo,
    ): PacketManager<*> =
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
                listeners.registerListener(GamesUpdateListener(serverManager))
            }.exception(SocketException::class.java, SocketExceptionHandler())
            .exception {
                logger.error(it) { "Netty Exception" }
            }.start()
}
