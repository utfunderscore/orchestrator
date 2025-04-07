package org.readutf.orchestrator.proxy

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.runCatching
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import org.jetbrains.annotations.Blocking
import org.readutf.orchestrator.common.template.ServiceTemplate
import org.readutf.orchestrator.common.template.TemplateBody
import org.readutf.orchestrator.common.template.TemplateName
import org.readutf.orchestrator.proxy.api.ServerService
import org.readutf.orchestrator.proxy.api.TemplateService
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.Executors

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
    private val serverService by lazy { retrofit.create(ServerService::class.java) }

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

    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    fun getTemplates() = scope.async { runCatching { templateService.listTemplates() } }

    fun getTemplate(name: String) = scope.async { runCatching { templateService.getTemplate(TemplateName(name)) } }

    fun deleteTemplate(name: String) = scope.async {
        runCatching {
            templateService.deleteTemplate(name)
        }
    }

    fun addTemplatePort(name: String, port: Int) = scope.async {
        runCatching {
            templateService.addPort(name, mapOf("port" to port))
        }
    }

    fun removeTemplatePort(name: String, port: Int) = scope.async {
        runCatching {
            templateService.removePort(name, mapOf("port" to port))
        }
    }

    fun setTemplateEnvironmentVariable(template: String, key: String, value: String) = scope.async {
        runCatching {
            templateService.setEnvironmentVariable(template, mapOf("key" to key, "value" to value))
        }
    }

    fun setTemplate(name: String, image: String) = scope.async {
        runCatching {
            templateService.setImage(name, mapOf("image" to image))
        }
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    fun getServers() = scope.async {
        runCatching {
            serverService.getServers()
        }
    }

    companion object {
        val objectMapper = jacksonObjectMapper { }
    }
}
