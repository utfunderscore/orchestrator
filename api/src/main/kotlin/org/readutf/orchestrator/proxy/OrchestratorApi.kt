package org.readutf.orchestrator.proxy

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.runCatching
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import org.jetbrains.annotations.Blocking
import org.readutf.orchestrator.common.server.Server
import org.readutf.orchestrator.common.template.ServiceTemplate
import org.readutf.orchestrator.common.template.TemplateBody
import org.readutf.orchestrator.common.template.TemplateName
import org.readutf.orchestrator.proxy.api.ServerFinderService
import org.readutf.orchestrator.proxy.api.TemplateService
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class OrchestratorApi(
    private val hostname: String,
) {

    private val dispatcher = Executors.newCachedThreadPool().asCoroutineDispatcher()
    private val scope = CoroutineScope(dispatcher)

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .addConverterFactory(JacksonConverterFactory.create(jacksonObjectMapper()))
            .baseUrl("http://orchestrator:9393/").build()
    }

    private val templateService by lazy { retrofit.create(TemplateService::class.java) }

    @Blocking
    fun createService(name: String, image: String, ports: List<Int>, environmentVariables: HashMap<String, String>): Deferred<Result<ServiceTemplate, Throwable>> = scope.async {
        runCatching {
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

    fun getTemplates() = scope.async { runCatching { templateService.listTemplates() } }

    fun getTemplate(name: String) = scope.async { runCatching { templateService.getTemplate(TemplateName(name)) } }

    fun setTemplate(name: String, image: String) = scope.async {
        runCatching {
            templateService.setImage(name, image)
        }
    }

    fun createTemplate(name: String, image: String, ports: List<Int>, environmentVariables: HashMap<String, String>) = scope.async {
        runCatching {
            templateService.createTemplate(
                name = name,
                templateBody = TemplateBody(
                    image = image,
                    ports = ports,
                    environmentVariables = environmentVariables,
                ),
            )
        }
    }

    fun findServer(
        serverType: String,
        connectionTimeout: Long = 2_000,
        findServerTimeout: Long = 10_000,
    ): CompletableFuture<Result<Server, Throwable>> {
        val future = CompletableFuture<Result<Server, Throwable>>()
        ServerFinderService(
            "ws://$hostname:$9393/serverfinder/$serverType",
            future,
        ).connectBlocking(connectionTimeout, TimeUnit.MILLISECONDS)
        return future.orTimeout(findServerTimeout, TimeUnit.MILLISECONDS)
    }

    companion object {
        val objectMapper = jacksonObjectMapper { }
    }
}
