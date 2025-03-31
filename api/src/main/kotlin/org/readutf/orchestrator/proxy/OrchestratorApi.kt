package org.readutf.orchestrator.proxy

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.runCatching
import kotlinx.coroutines.runBlocking
import org.jetbrains.annotations.Blocking
import org.readutf.orchestrator.common.server.Server
import org.readutf.orchestrator.common.template.ServiceTemplate
import org.readutf.orchestrator.common.template.TemplateBody
import org.readutf.orchestrator.proxy.api.ServerFinderService
import org.readutf.orchestrator.proxy.api.TemplateService
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class OrchestratorApi(
    private val hostname: String,
) {

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .addConverterFactory(JacksonConverterFactory.create(jacksonObjectMapper()))
            .baseUrl("http://$hostname:9191/").build()
    }

    private val templateService by lazy { retrofit.create(TemplateService::class.java) }

    @Blocking
    fun createService(name: String, image: String, ports: List<Int>, environmentVariables: Map<String, String>): Result<ServiceTemplate, Throwable> = runCatching {
        runBlocking {
            templateService.createTemplate(
                name,
                TemplateBody(
                    image = image,
                    ports = ports,
                    environmentVariables = environmentVariables,
                ),
            )
        }
    }

    fun findServerBlocking(
        serverType: String,
        connectionTimeout: Long = 2_000,
        findServerTimeout: Long = 10_000,
    ): CompletableFuture<Result<Server, Throwable>> {
        val future = CompletableFuture<Result<Server, Throwable>>()
        ServerFinderService(
            "ws://$hostname:$9191/serverfinder/$serverType",
            future,
        ).connectBlocking(connectionTimeout, TimeUnit.MILLISECONDS)
        return future.orTimeout(findServerTimeout, TimeUnit.MILLISECONDS)
    }

    companion object {
        val objectMapper = jacksonObjectMapper { }
    }
}
