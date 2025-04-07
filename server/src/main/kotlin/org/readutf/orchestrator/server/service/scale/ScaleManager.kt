package org.readutf.orchestrator.server.service.scale

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.orchestrator.common.template.TemplateName
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.server.service.platform.ContainerPlatform
import org.readutf.orchestrator.server.service.template.TemplateManager
import java.time.Duration
import java.time.Instant
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ScaleManager(
    private val serverManager: ServerManager,
    private val containerManager: ContainerPlatform,
    private val templateManager: TemplateManager,
) {
    private val logger = KotlinLogging.logger {}
    private val targetScales = mutableMapOf<TemplateName, Int>()
    private val lastScaledTimes = mutableMapOf<TemplateName, Long>()
    private val executorServices = Executors.newSingleThreadScheduledExecutor()

    init {
        logger.info { "Starting scale manager..." }

        executorServices.scheduleAtFixedRate({
            targetScales.toMap().keys.forEach { scale ->
                scaleService(scale)
            }
        }, 5, 1, TimeUnit.SECONDS)
    }

    fun scaleService(
        templateName: TemplateName,
        scale: Int,
    ): Result<Unit, Throwable> {
        if (scale < 0) return Err(Exception("Scale cannot be less than 0"))

        targetScales[templateName] = scale

        return Ok(Unit)
    }

    fun getScale(templateId: TemplateName): Int = targetScales.getOrPut(templateId) { 0 }

    private fun scaleService(templateId: TemplateName) {
        val template = templateManager.get(templateId) ?: run {
            logger.error { "Could not find template $templateId" }
            targetScales.remove(templateId)
            lastScaledTimes.remove(templateId)
            return
        }

        // Skip scaling if it was scaled in the last 15 seconds
        if (System.currentTimeMillis() - lastScaledTimes.getOrPut(templateId) { 0 } < 15_000) {
            logger.debug { "Skipping scaling for $templateId" }
            return
        }

        val targetScale = targetScales.getOrPut(templateId) { 0 }

        val serversByTemplate = serverManager.getServers().filter {
            containerManager.getTemplate(it.shortContainerId) == template.name
        }

        val pending = containerManager.getContainers()
            .filter { container ->
                serversByTemplate.none { it.shortContainerId == container.containerId }
            }
            .filter { Duration.between(Instant.now(), it.createdAt).abs().seconds < 15 }

        val currentScale = serversByTemplate.size + pending.size

        val neededServers = targetScale - currentScale
        logger.debug { "Needed servers: $neededServers" }

        if (neededServers == 0) {
            logger.debug { "Target scale already met" }
            return
        }

        lastScaledTimes[templateId] = System.currentTimeMillis()

        if (neededServers > 0) {
            // Scaling deployment up
            logger.debug { "Scaling deployment up" }

            for (i in 0 until neededServers) {
                containerManager.createContainer(template).onFailure {
                    logger.warn { "Failed to create container $it" }
                    return
                }
            }
        } else {
            // Scaling deployment down
            val serversToRemove = -neededServers

            if (serversByTemplate.size < targetScale) {
                logger.debug { "Cannot scale down yet, servers are still being created" }
                return
            }

            for (registeredServer in serversByTemplate.take(serversToRemove).sortedBy { it.getCapacity() }) {
                serverManager.scheduleShutdown(registeredServer)
            }
        }
    }
}
