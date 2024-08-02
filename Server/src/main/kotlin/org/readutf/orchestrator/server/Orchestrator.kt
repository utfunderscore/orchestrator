package org.readutf.orchestrator.server

import com.esotericsoftware.kryo.kryo5.Kryo
import io.github.oshai.kotlinlogging.KotlinLogging
import io.javalin.Javalin
import org.readutf.hermes.PacketManager
import org.readutf.hermes.platform.netty.nettyServer
import org.readutf.hermes.serializer.KryoPacketSerializer
import org.readutf.orchestrator.server.game.GameManager
import org.readutf.orchestrator.server.game.store.impl.InMemoryGameStore
import org.readutf.orchestrator.server.network.exception.SocketExceptionHandler
import org.readutf.orchestrator.server.network.listeners.ChannelCloseListener
import org.readutf.orchestrator.server.network.listeners.GamesUpdateListener
import org.readutf.orchestrator.server.network.listeners.HeartbeatListener
import org.readutf.orchestrator.server.network.listeners.ServerRegisterListener
import org.readutf.orchestrator.server.network.listeners.ServerUnregisterListener
import org.readutf.orchestrator.server.server.ServerCommand
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.server.server.store.impl.MemoryServerStore
import org.readutf.orchestrator.server.settings.Settings
import org.readutf.orchestrator.server.utils.FastJsonMapper
import org.readutf.orchestrator.shared.kryo.KryoCreator
import revxrsal.commands.cli.ConsoleCommandHandler
import java.net.SocketException

class Orchestrator(
    val settings: Settings,
) {
    private val logger = KotlinLogging.logger { }

    private val commandManager: ConsoleCommandHandler = ConsoleCommandHandler.create()

    init {
        val kryo = KryoCreator.build()
        val javalin = setupJavalin()
        val serverStore = MemoryServerStore()
        val serverManager = ServerManager(serverStore)
        val gameManager = GameManager(javalin, serverManager, InMemoryGameStore(serverStore))
        val packetManager = setupPacketManager(serverManager, gameManager, kryo)

        Thread({
            commandManager.register(ServerCommand(serverManager, gameManager))
            commandManager.pollInput()
        }, "Command Thread").start()
    }

    private fun setupJavalin() =
        Javalin.createAndStart {
            it.jetty.defaultHost = settings.apiSettings.host
            it.jetty.defaultPort = settings.apiSettings.port
            it.jsonMapper(FastJsonMapper)
            it.useVirtualThreads = settings.apiSettings.virtualThreads
            it.showJavalinBanner = false
        }

    private fun setupPacketManager(
        serverManager: ServerManager,
        gameManager: GameManager,
        kryo: Kryo,
    ): PacketManager<*> =
        PacketManager
            .nettyServer(
                hostName = settings.serversettings.host,
                port = settings.serversettings.port,
                serializer = KryoPacketSerializer(kryo),
            ).editListeners { listeners ->
                listeners.registerListener(ChannelCloseListener(serverManager))
                listeners.registerListener(HeartbeatListener(serverManager))
                listeners.registerListener(ServerRegisterListener(serverManager))
                listeners.registerListener(ServerUnregisterListener(serverManager))
                listeners.registerListener(GamesUpdateListener(gameManager))
            }.exception(SocketException::class.java, SocketExceptionHandler())
            .exception {
                logger.error(it) { "Netty Exception" }
            }.start()
}
