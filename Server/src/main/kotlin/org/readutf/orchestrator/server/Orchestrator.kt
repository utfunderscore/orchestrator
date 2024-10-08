package org.readutf.orchestrator.server

import com.esotericsoftware.kryo.kryo5.Kryo
import com.esotericsoftware.kryo.kryo5.util.Pool
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.hermes.PacketManager
import org.readutf.hermes.platform.netty.nettyServer
import org.readutf.hermes.serializer.KryoPacketSerializer
import org.readutf.orchestrator.server.api.EndpointManager
import org.readutf.orchestrator.server.game.GameManager
import org.readutf.orchestrator.server.game.store.impl.InMemoryGameStore
import org.readutf.orchestrator.server.network.exception.SocketExceptionHandler
import org.readutf.orchestrator.server.network.listeners.ChannelCloseListener
import org.readutf.orchestrator.server.network.listeners.game.GamesUpdateListener
import org.readutf.orchestrator.server.server.ServerCommand
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.server.server.listeners.AttributeUpdateListener
import org.readutf.orchestrator.server.server.listeners.HeartbeatListener
import org.readutf.orchestrator.server.server.listeners.ServerRegisterListener
import org.readutf.orchestrator.server.server.listeners.ServerUnregisterListener
import org.readutf.orchestrator.server.server.store.impl.MemoryServerStore
import org.readutf.orchestrator.server.settings.Settings
import org.readutf.orchestrator.shared.kryo.KryoCreator
import revxrsal.commands.cli.ConsoleCommandHandler
import java.net.SocketException
import java.util.concurrent.Executors

class Orchestrator(
    private val settings: Settings,
) {
    private val logger = KotlinLogging.logger { }

    private val commandManager: ConsoleCommandHandler = ConsoleCommandHandler.create()

    init {
        val kryo = KryoCreator.build()
        val serverStore = MemoryServerStore()
        val serverManager = ServerManager(serverStore)
        val gameManager = GameManager(InMemoryGameStore(serverStore))
        val endpointManager =
            EndpointManager(
                settings = settings,
                serverManager = serverManager,
                gameManager = gameManager,
            )

        setupPacketManager(serverManager, gameManager, kryo)

        Thread({
            commandManager.register(ServerCommand(kryo, serverManager, gameManager))
            commandManager.pollInput()
        }, "Command Thread").start()

        // shutdown hook
        Runtime.getRuntime().addShutdownHook(
            Thread {
                logger.info { "Shutting down Orchestrator" }
                endpointManager.shutdown()
            },
        )
    }

    private fun setupPacketManager(
        serverManager: ServerManager,
        gameManager: GameManager,
        kryo: Kryo,
    ): PacketManager<*> =
        PacketManager
            .nettyServer(
                hostName = settings.serverSettings.host,
                port = settings.serverSettings.port,
                serializer =
                    KryoPacketSerializer(
                        object : Pool<Kryo>(true, false, 16) {
                            override fun create(): Kryo = KryoCreator.build()
                        },
                    ),
                executorService = Executors.newSingleThreadExecutor(),
            ).editListeners { listeners ->
                listeners.registerListener(ChannelCloseListener(serverManager))
                listeners.registerListener(HeartbeatListener(serverManager))
                listeners.registerListener(ServerRegisterListener(serverManager))
                listeners.registerListener(ServerUnregisterListener(serverManager))
                listeners.registerListener(GamesUpdateListener(gameManager))
                listeners.registerListener(AttributeUpdateListener(serverManager))
            }.exception(SocketException::class.java, SocketExceptionHandler())
            .exception {
                logger.error(it) { "Netty Exception" }
            }.start()

    companion object {
        val objectMapper = ObjectMapper().registerKotlinModule()
    }
}
