package org.readutf.orchestrator.wrapper

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.TypeReference
import kotlinx.coroutines.runBlocking
import org.jetbrains.annotations.Blocking
import org.readutf.orchestrator.shared.utils.ApiResponse
import org.readutf.orchestrator.wrapper.services.DockerService
import org.readutf.orchestrator.wrapper.services.ServerService
import org.readutf.orchestrator.wrapper.types.ContainerPort
import retrofit2.Retrofit
import retrofit2.converter.fastjson.FastJsonConverterFactory
import java.util.Base64

class OrchestratorApi(
    hostname: String,
    port: Int,
) {
    private val requestClient by lazy { GameRequestClient("ws://$hostname:$port/game/request") }

    private val retrofit: Retrofit =
        Retrofit
            .Builder()
            .baseUrl("http://$hostname:$port")
            .addConverterFactory(FastJsonConverterFactory.create())
            .build()

    val serverService by lazy { retrofit.create(ServerService::class.java) }
    val dockerService by lazy { retrofit.create(DockerService::class.java) }

    @Blocking
    fun requestGame(
        gameType: String,
        timeout: Long = 5000,
    ) = requestClient.requestGame(gameType, timeout)

    @Blocking
    fun getPort(shortId: String) =
        runBlocking {
            val json = String(Base64.getDecoder().decode(dockerService.getPort(shortId)))
            JSON.parseObject(json, object : TypeReference<ApiResponse<List<ContainerPort>>>() {})
        }
}
