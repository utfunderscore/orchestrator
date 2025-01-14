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
import org.jetbrains.exposed.sql.Database
import org.readutf.hermes.PacketManager
import org.readutf.hermes.platform.netty.NettyServerPlatform
import org.readutf.hermes.platform.netty.nettyServer
import org.readutf.hermes.serializer.KryoPacketSerializer
import org.readutf.orchestrator.common.packets.KryoBuilder
import org.readutf.orchestrator.server.container.ContainerController
import org.readutf.orchestrator.server.container.impl.docker.DockerController
import org.readutf.orchestrator.server.container.impl.docker.store.DockerTemplateStore
import org.readutf.orchestrator.server.container.impl.docker.store.exposed.ExposedTemplateStore
import org.readutf.orchestrator.server.container.scale.ScaleEndpoints
import org.readutf.orchestrator.server.container.scale.ScaleManager
import org.readutf.orchestrator.server.loadbalancer.LoadBalancerManager
import org.readutf.orchestrator.server.server.ServerEndpoints
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.server.server.listeners.ServerDisconnectListener
import org.readutf.orchestrator.server.server.listeners.ServerHeartbeatListener
import org.readutf.orchestrator.server.server.listeners.ServerRegisterListener
import org.readutf.orchestrator.server.serverfinder.ServerFinderEndpoint
import org.readutf.orchestrator.server.serverfinder.ServerFinderManager
import java.util.concurrent.Executors

class Orchestrator(
    private val hostAddress: String,
) {
    private val dockerTemplateStore: DockerTemplateStore =
        ExposedTemplateStore(
            Database.connect(
                "jdbc:postgresql://postgres:5432/orchestrator",
                driver = "org.postgresql.Driver",
                user = "orchestrator",
                password = "orchestrator",
            ),
        )
    private val dockerClient = createDockerClient("unix:///var/run/docker.sock")
    private val dockerController: ContainerController<*> = DockerController(dockerClient, dockerTemplateStore)
    private val serverManager = ServerManager(dockerController)
    private val scaleManager = ScaleManager(serverManager, dockerController)
    private val loadBalancerManager = LoadBalancerManager(serverManager, scaleManager)
    private val serverFinderManager = ServerFinderManager(loadBalancerManager, serverManager)

    private val serverEndpoints = ServerEndpoints(serverManager)
    private val scaleEndpoints = ScaleEndpoints(scaleManager)
    private val serverFinderEndpoint = ServerFinderEndpoint(serverFinderManager, dockerController)

    private val logger = KotlinLogging.logger {}

    init {
        val javalin = setupJavalin(dockerController, scaleEndpoints)
        logger.info { "Web api started at $hostAddress:9191" }
        val hermes = setupHermes(hostAddress, serverManager)
        logger.info { "Hermes started at $hostAddress:2323" }

        Runtime.getRuntime().addShutdownHook(
            Thread {
                javalin.stop()
                hermes.stop()
            },
        )
    }

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

    private fun setupJavalin(
        containerController: ContainerController<*>,
        scaleEndpoints: ScaleEndpoints,
    ): Javalin {
        val javalin =
            Javalin.create { config ->
                config.useVirtualThreads = true
                config.http.asyncTimeout = 10_000
                config.bundledPlugins.enableDevLogging()
                config.showJavalinBanner = false
            }

        containerController.registerEndpoints(javalin)

        javalin.post("/scale/{id}", scaleEndpoints::scaleServer)
        javalin.get("/servers/", serverEndpoints::listServers)
        javalin.ws("/serverfinder/{type}", serverFinderEndpoint.ServerFinderSocket())
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

    companion object {
        val objectMapper: ObjectMapper =
            jacksonObjectMapper { }
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
    }
}
