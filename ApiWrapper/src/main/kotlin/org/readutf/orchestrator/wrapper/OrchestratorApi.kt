package org.readutf.orchestrator.wrapper

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.jetbrains.annotations.Blocking
import org.readutf.orchestrator.shared.server.Server
import org.readutf.orchestrator.shared.utils.ApiResponse
import org.readutf.orchestrator.shared.utils.Result
import org.readutf.orchestrator.wrapper.services.DockerService
import org.readutf.orchestrator.wrapper.services.ServerService
import org.readutf.orchestrator.wrapper.types.ContainerPort
import org.readutf.orchestrator.wrapper.types.NetworkAddress
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.Base64
import java.util.UUID

class OrchestratorApi(
    hostname: String,
    port: Int,
) {
    private val requestClient by lazy { GameRequestClient("ws://$hostname:$port/game/request") }

    private val retrofit: Retrofit =
        Retrofit
            .Builder()
            .baseUrl("http://$hostname:$port")
            .addConverterFactory(JacksonConverterFactory.create(objectMapper))
            .build()

    private val serverService by lazy { retrofit.create(ServerService::class.java) }
    private val dockerService by lazy { retrofit.create(DockerService::class.java) }

    @Blocking
    fun requestGame(
        gameType: String,
        timeout: Long = 5000,
    ) = requestClient.requestGame(gameType, timeout)

    suspend fun getPort(shortId: String): ApiResponse<List<ContainerPort>> {
        val json = String(Base64.getDecoder().decode(dockerService.getPort(shortId)))
        return objectMapper.readValue(json, object : TypeReference<ApiResponse<List<ContainerPort>>>() {})
    }

    suspend fun getNetworks(shortId: String): ApiResponse<List<NetworkAddress>> = dockerService.getIp(shortId)

    suspend fun getServerByType(gameType: String): Result<List<Server>> =
        try {
            val response = serverService.getServer(gameType)
            if (response.isSuccess()) {
                Result.ok(response.get())
            } else {
                Result.error(response.getError())
            }
        } catch (e: Exception) {
            Result.error(e.message.toString())
        }

    suspend fun getServerById(serverId: UUID): Result<Server> =
        try {
            val response = serverService.getServer(serverId)
            if (response.isSuccess()) {
                Result.ok(response.get())
            } else {
                Result.error(response.getError())
            }
        } catch (e: Exception) {
            Result.error(e.message.toString())
        }

    suspend fun getServers(): Result<List<Server>> =
        try {
            val response = serverService.getAllServers()
            if (response.isSuccess()) {
                Result.ok(response.get())
            } else {
                Result.error(response.getError())
            }
        } catch (e: Exception) {
            Result.error(e.message.toString())
        }

    companion object {
        val objectMapper = ObjectMapper().registerKotlinModule()
    }
}
