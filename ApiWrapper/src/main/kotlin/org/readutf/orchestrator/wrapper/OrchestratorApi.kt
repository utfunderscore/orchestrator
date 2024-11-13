package org.readutf.orchestrator.wrapper

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.jetbrains.annotations.Blocking
import org.readutf.orchestrator.shared.notification.Notification
import org.readutf.orchestrator.shared.server.Server
import org.readutf.orchestrator.shared.utils.ApiResponse
import org.readutf.orchestrator.shared.utils.Result
import org.readutf.orchestrator.wrapper.services.DockerService
import org.readutf.orchestrator.wrapper.services.LoadBalancerService
import org.readutf.orchestrator.wrapper.services.ServerService
import org.readutf.orchestrator.wrapper.socket.GameRequestSocket
import org.readutf.orchestrator.wrapper.socket.NotificationSocket
import org.readutf.orchestrator.wrapper.types.ContainerPort
import org.readutf.orchestrator.wrapper.types.NetworkAddress
import org.readutf.orchestrator.wrapper.utils.JsonWrapper
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.*

class OrchestratorApi(
    val hostname: String,
    val port: Int,
) {
    private val requestClient by lazy { GameRequestSocket("ws://$hostname:$port/game/request") }

    private val retrofit: Retrofit =
        getRetrofit(hostname, port)

    private val serverService by lazy { retrofit.create(ServerService::class.java) }
    private val dockerService by lazy { retrofit.create(DockerService::class.java) }
    private val loadBalancerServer by lazy { retrofit.create(LoadBalancerService::class.java) }

    @Blocking
    fun requestGame(
        gameType: String,
        timeout: Long = 5000,
    ) = requestClient.requestGame(gameType, timeout)

    fun createNotificationSocket(
        objectMapper: ObjectMapper = ObjectMapper(),
        listener: (Notification) -> Unit,
    ): NotificationSocket =
        NotificationSocket(
            uri = "ws://$hostname:$port/notifications",
            objectMapper = objectMapper,
            notificationListener = listener,
        )

    suspend fun getPort(shortId: String): ApiResponse<List<ContainerPort>> {
        val json = String(Base64.getDecoder().decode(dockerService.getPort(shortId)))
        return objectMapper.readValue(json, object : TypeReference<ApiResponse<List<ContainerPort>>>() {})
    }

    suspend fun getServerFromBalancer(
        serverType: String,
        players: List<UUID>,
    ): ApiResponse<Server> = loadBalancerServer.getServerFromBalancer(serverType, JsonWrapper(players))

    suspend fun getIp(shortId: String): ApiResponse<List<NetworkAddress>> = dockerService.getIp(shortId)

    suspend fun getNetworks(shortId: String): ApiResponse<List<NetworkAddress>> = dockerService.getIp(shortId)

    suspend fun getServerByType(gameType: String): Result<List<Server>, String> =
        try {
            val response = serverService.getServerByType(gameType)
            if (response.isSuccess()) {
                Result.success(response.get())
            } else {
                Result.failure(response.getError())
            }
        } catch (e: Exception) {
            Result.failure(e.message.toString())
        }

    suspend fun getServerById(serverId: String): Result<Server, String> =
        try {
            val response = serverService.getServer(serverId)
            if (response.isSuccess()) {
                Result.success(response.get())
            } else {
                Result.failure(response.getError())
            }
        } catch (e: Exception) {
            Result.failure(e.message.toString())
        }

    suspend fun getServers(): Result<List<Server>, String> =
        try {
            val response = serverService.getAllServers()
            if (response.isSuccess()) {
                Result.success(response.get())
            } else {
                Result.failure(response.getError())
            }
        } catch (e: Exception) {
            Result.failure(e.message.toString())
        }

    private fun getRetrofit(
        hostname: String,
        port: Int,
    ): Retrofit {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client: OkHttpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()

        return Retrofit
            .Builder()
            .baseUrl("http://$hostname:$port")
            .addConverterFactory(JacksonConverterFactory.create(objectMapper))
            .client(client)
            .build()
    }

    companion object {
        val objectMapper = ObjectMapper().registerKotlinModule()
    }
}
