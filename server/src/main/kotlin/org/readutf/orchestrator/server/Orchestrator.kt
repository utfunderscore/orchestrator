package org.readutf.orchestrator.server

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.util.Pool
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient
import io.github.oshai.kotlinlogging.KotlinLogging
import io.javalin.Javalin
import org.readutf.hermes.PacketManager
import org.readutf.hermes.platform.netty.NettyServerPlatform
import org.readutf.hermes.platform.netty.nettyServer
import org.readutf.hermes.serializer.KryoPacketSerializer
import org.readutf.orchestrator.common.packets.KryoBuilder
import org.readutf.orchestrator.server.container.ContainerController
import org.readutf.orchestrator.server.container.command.DockerCommands
import org.readutf.orchestrator.server.container.command.TemplateCommands
import org.readutf.orchestrator.server.container.impl.docker.DockerController
import org.readutf.orchestrator.server.container.impl.docker.store.DockerTemplateStore
import org.readutf.orchestrator.server.container.scale.ScaleManager
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.server.server.listeners.ServerDisconnectListener
import org.readutf.orchestrator.server.server.listeners.ServerHeartbeatListener
import org.readutf.orchestrator.server.server.listeners.ServerRegisterListener
import revxrsal.commands.Lamp
import revxrsal.commands.cli.CLILamp
import revxrsal.commands.cli.ConsoleActor
import java.io.File
import java.util.concurrent.Executors

class Orchestrator(
    private val hostAddress: String,
) {
    private val dockerTemplateStore = DockerTemplateStore(File("docker-templates.json"))
    private val dockerClient = createDockerClient("unix:///var/run/docker.sock")
    private val dockerController = DockerController(dockerClient, dockerTemplateStore)
    private val serverManager = ServerManager(dockerController)
    private val scaleManager = ScaleManager(serverManager, dockerController)

    private val logger = KotlinLogging.logger {}

    init {
        val javalin = setupJavalin(dockerController)
        logger.info { "Web api started at $hostAddress:9191" }
        val hermes = setupHermes(hostAddress, serverManager)
        logger.info { "Hermes started at $hostAddress:2323" }
        val lamp = startCommandManager(dockerController)
        logger.info { "Command manager started" }

        Runtime.getRuntime().addShutdownHook(
            Thread {
                javalin.stop()
                hermes.stop()
            },
        )
    }

    val lamp = startCommandManager(dockerController)

    private fun setupHermes(
        hostAddress: String,
        serverManager: ServerManager,
    ): PacketManager<NettyServerPlatform> {
        val pool =
            object : Pool<Kryo>(true, true) {
                override fun create(): Kryo = KryoBuilder.build()
            }

        val nettyServer =
            PacketManager.nettyServer(
                hostAddress,
                port = 2323,
                serializer = KryoPacketSerializer(pool),
                executorService = Executors.newCachedThreadPool(),
            )
        nettyServer.start()

        nettyServer.editListeners {
            it.registerListener(ServerRegisterListener(serverManager))
            it.registerListener(ServerHeartbeatListener(serverManager))
            it.registerListener(ServerDisconnectListener(serverManager))
        }

        return nettyServer
    }

    private fun setupJavalin(containerController: ContainerController<*>): Javalin {
        val javalin =
            Javalin.create { config ->
                config.useVirtualThreads = true
                config.http.asyncTimeout = 10_000
            }

        containerController.registerEndpoints(javalin)

        javalin.start(hostAddress, 9191)

        return javalin
    }

    private fun createDockerClient(dockerHost: String): DockerClient {
        val config =
            DefaultDockerClientConfig
                .createDefaultConfigBuilder()
                .withDockerHost(dockerHost)
                .build()

        val client =
            DockerClientImpl.getInstance(
                config,
                ZerodepDockerHttpClient
                    .Builder()
                    .dockerHost(config.dockerHost)
                    .build(),
            )

        return client
    }

    private fun startCommandManager(containerController: ContainerController<*>): Lamp<ConsoleActor>? {
        val lamp =
            CLILamp
                .builder<ConsoleActor>()
                .build()

        lamp.register(DockerCommands(containerController))
        lamp.register(TemplateCommands(containerController, scaleManager))

        Thread({
            lamp.accept(CLILamp.pollStdin())
        }, "Lamp-Cli-Thread").start()

        return lamp
    }

    companion object {
        val objectMapper: ObjectMapper =
            jacksonObjectMapper { }
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
    }
}
