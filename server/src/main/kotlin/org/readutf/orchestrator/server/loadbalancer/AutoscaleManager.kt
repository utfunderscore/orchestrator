package org.readutf.orchestrator.server.loadbalancer

import com.fasterxml.jackson.databind.JsonNode
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.flatMap
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.github.michaelbull.result.toResultOr
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.exposed.sql.Database
import org.readutf.orchestrator.common.template.TemplateName
import org.readutf.orchestrator.server.loadbalancer.store.AutoscaleSerializer
import org.readutf.orchestrator.server.loadbalancer.store.impl.FixedAutoscaleSerializer
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.server.service.scale.ScaleManager
import org.readutf.orchestrator.server.service.template.TemplateManager
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class AutoscaleManager(
    database: Database,
    serverManager: ServerManager,
    templateManager: TemplateManager,
    scaleManager: ScaleManager,
) {
    private val logger = KotlinLogging.logger { }
    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    val scalers: MutableMap<TemplateName, Autoscaler> = mutableMapOf()
    private val serializers: MutableMap<String, AutoscaleSerializer> = mutableMapOf()

    init {
        logger.info { "Starting load balancer..." }
        executor.scheduleAtFixedRate(AutoscaleTask(this, scaleManager, serverManager, templateManager), 0, 1, TimeUnit.SECONDS)
        serializers["fixed_count"] = FixedAutoscaleSerializer(database)
        loadScalers()
    }

    private fun loadScalers() {
        for ((name, store) in serializers) {
            store.load().onFailure {
                logger.error(it) { "Failed to load autoscalers for $name" }
            }.onSuccess {
                logger.info { "Loaded ${it.size} autoscalers for $name" }
                for ((name, scaler) in it) {
                    scalers[name] = scaler
                }
            }
        }
    }

    fun saveScalers() {
        logger.info { "Shutting down load balancer..." }
        executor.shutdownNow()
        for ((name, store) in serializers) {
            val byType = scalers.filterValues { it.name.equals(name, true) }
            store.save(byType).onSuccess {
                logger.info { "Saved ${byType.size} autoscalers for $name" }
            }.onFailure {
                logger.error(it) { "Failed to save autoscalers for $name" }
            }
        }
    }

    fun getSerializer(name: String): AutoscaleSerializer? = serializers[name]

    fun createScaler(type: String, jsonNode: JsonNode): Result<Autoscaler, Throwable> = serializers[type]
        .toResultOr { Throwable("Serializer for $type not found") }
        .flatMap { it.create(jsonNode) }

    fun getScaler(templateName: TemplateName) = scalers[templateName]

    fun setScaler(name: TemplateName, autoScaler: Autoscaler) {
        scalers[name] = autoScaler
    }
}
