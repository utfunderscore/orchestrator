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
import org.readutf.orchestrator.server.api.TemplateEndpoints
import org.readutf.orchestrator.server.features.games.GameManager
import org.readutf.orchestrator.server.loadbalancer.AutoscaleManager
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.server.server.listeners.ServerDisconnectListener
import org.readutf.orchestrator.server.server.listeners.ServerHeartbeatListener
import org.readutf.orchestrator.server.server.listeners.ServerRegisterListener
import org.readutf.orchestrator.server.server.listeners.UpdateAttributesListener
import org.readutf.orchestrator.server.serverfinder.ServerFinderManager
import org.readutf.orchestrator.server.service.platform.docker.DockerContainerPlatform
import org.readutf.orchestrator.server.service.scale.ScaleManager
import org.readutf.orchestrator.server.service.template.TemplateManager
import org.readutf.orchestrator.server.service.template.store.impl.SqlTemplateStore
import java.util.concurrent.Executors

class Orchestrator(hostAddress: String) {
    private val logger = KotlinLogging.logger {}

    val database = Database.connect(
        "jdbc:postgresql://postgres:5432/orchestrator",
        driver = "org.postgresql.Driver",
        user = "orchestrator",
        password = "orchestrator",
    )

    private val templateStore: SqlTemplateStore = SqlTemplateStore(database)

    private val dockerClient = createDockerClient(System.getenv("DOCKER_HOST") ?: "unix:///var/run/docker.sock")
    private val containerPlatform = DockerContainerPlatform(dockerClient, database)
    private val serverManager = ServerManager(containerPlatform)
    private val templateManager = TemplateManager(templateStore)
    private val scaleManager = ScaleManager(serverManager, containerPlatform, templateManager)
    private val autoscaleManager = AutoscaleManager(database, serverManager, scaleManager)
    private val serverFinderManager = ServerFinderManager(autoscaleManager, serverManager)

    // Extra services
    private val gameManager = GameManager(serverManager, objectMapper)
    private val javalin = setupJavalin()

    init {
        val hermes = setupHermes(hostAddress, serverManager)
        val templateEndpoints = TemplateEndpoints(templateManager)

        javalin.post("/api/template/{name}", templateEndpoints.templateCreateHandler)
        javalin.put("/api/template/{name}/port", templateEndpoints.addPortHandler)
        javalin.delete("/api/template/{name}/port", templateEndpoints.removePortHandler)
        javalin.put("/api/template/{name}/image", templateEndpoints.setImageHandler)
        javalin.put("/api/template/{name}/env", templateEndpoints.setEnvironmentVariableHandler)
        javalin.delete("/api/template/{name}/env", templateEndpoints.removeEnvironmentVariableHandler)
        javalin.get("/api/template/{name}", templateEndpoints.templateGetHandler)
        javalin.get("/api/template", templateEndpoints.templateListHandler)

        javalin.start("0.0.0.0", 9393)

        Runtime.getRuntime().addShutdownHook(
            Thread {
                javalin.stop()
                hermes.stop()
                autoscaleManager.saveScalers()
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

    private fun setupJavalin(): Javalin = Javalin.create { config ->
        config.useVirtualThreads = true
        config.http.asyncTimeout = 10_000
        config.showJavalinBanner = false
    }.after { ctx ->
        logger.info { "[${ctx.method()}] ${ctx.fullUrl()} - ${ctx.statusCode()}" }
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
