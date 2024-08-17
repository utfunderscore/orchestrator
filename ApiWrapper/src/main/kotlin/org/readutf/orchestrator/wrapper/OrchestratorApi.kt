package org.readutf.orchestrator.wrapper

import retrofit2.Retrofit
import retrofit2.converter.fastjson.FastJsonConverterFactory

class OrchestratorApi(
    hostname: String,
    port: Int,
) {
    private val requestClient by lazy { GameRequestClient("ws://$hostname:$port/game/request") }

    private val retrofit: Retrofit =
        Retrofit
            .Builder()
            .baseUrl("http://localhost:9393")
            .addConverterFactory(FastJsonConverterFactory.create())
            .build()

    val serverService by lazy { retrofit.create(ServerService::class.java) }

    fun requestGame(
        gameType: String,
        timeout: Long = 5000,
    ) = requestClient.requestGame(gameType, timeout)
}
