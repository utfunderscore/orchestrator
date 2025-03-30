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
import org.readutf.orchestrator.server.container.ContainerEndpoints
import org.readutf.orchestrator.server.container.ContainerManager
import org.readutf.orchestrator.server.container.impl.docker.DockerController
import org.readutf.orchestrator.server.container.impl.docker.store.DockerTemplateStore
import org.readutf.orchestrator.server.container.impl.docker.store.exposed.ExposedTemplateStore
import org.readutf.orchestrator.server.container.scale.ScaleEndpoints
import org.readutf.orchestrator.server.container.scale.ScaleManager
import org.readutf.orchestrator.server.features.games.GameManager
import org.readutf.orchestrator.server.features.games.api.GameFinderEndpoint
import org.readutf.orchestrator.server.loadbalancer.LoadBalancerManager
import org.readutf.orchestrator.server.server.ServerEndpoints
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.server.server.listeners.ServerDisconnectListener
import org.readutf.orchestrator.server.server.listeners.ServerHeartbeatListener
import org.readutf.orchestrator.server.server.listeners.ServerRegisterListener
import org.readutf.orchestrator.server.server.listeners.UpdateAttributesListener
import org.readutf.orchestrator.server.serverfinder.ServerFinderEndpoint
import org.readutf.orchestrator.server.serverfinder.ServerFinderManager
import org.readutf.orchestrator.server.utils.ws
import java.util.concurrent.Executors

class Orchestrator(
    private val hostAddress: String,
) {
    private val logger = KotlinLogging.logger {}

    private val dockerTemplateStore: DockerTemplateStore =
        ExposedTemplateStore(
            Database.connect(
                "jdbc:postgresql://postgres:5432/orchestrator",
                driver = "org.postgresql.Driver",
                user = "orchestrator",
                password = "orchestrator",
            ),
        )

    private val dockerClient = createDockerClient(System.getenv("DOCKER_HOST") ?: "unix:///var/run/docker.sock")
    private val dockerController: ContainerManager<*> = DockerController(dockerClient, dockerTemplateStore)
    private val serverManager = ServerManager(dockerController)
    private val scaleManager = ScaleManager(serverManager, dockerController)
    private val loadBalancerManager = LoadBalancerManager(serverManager, scaleManager)
    private val serverFinderManager = ServerFinderManager(loadBalancerManager, serverManager)

    private val serverEndpoints = ServerEndpoints(serverManager)
    private val scaleEndpoints = ScaleEndpoints(scaleManager)
    private val serverFinderEndpoint = ServerFinderEndpoint(serverFinderManager, dockerController)
    private val gameManager = GameManager(serverManager, objectMapper)

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
            it.registerListener(UpdateAttributesListener(serverManager))
        }

        return nettyServer
    }

    private fun setupJavalin(
        containerManager: ContainerManager<*>,
        scaleEndpoints: ScaleEndpoints,
    ): Javalin {
        val javalin =
            Javalin.create { config ->
                config.useVirtualThreads = true
                config.http.asyncTimeout = 10_000
                config.showJavalinBanner = false
            }.after { ctx ->
                logger.info { "[${ctx.method()}] ${ctx.fullUrl()} - ${ctx.statusCode()}" }
            }

        val containerEndpoints = ContainerEndpoints(containerManager)

        javalin.post("/scale/{id}", scaleEndpoints::scaleServer)
        javalin.get("/servers/", serverEndpoints::listServers)
        javalin.get("/template", containerEndpoints.getTemplatesEndpoint)
        javalin.put("/template/{name}", containerEndpoints.createServiceEndpoint)
        javalin.delete("/template/{name}", containerEndpoints.deleteServiceEndpoint)
        javalin.ws("/serverfinder/{type}", serverFinderEndpoint.ServerFinderSocket())
        javalin.ws("/gamefinder/{type}/", GameFinderEndpoint(gameManager))
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
