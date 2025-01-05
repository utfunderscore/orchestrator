package org.readutf.orchestrator.server.container.scale

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.onFailure
import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.orchestrator.common.utils.SResult
import org.readutf.orchestrator.server.container.ContainerController
import org.readutf.orchestrator.server.server.ServerManager
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ScaleManager(
    private val serverManager: ServerManager,
    private val containerController: ContainerController<*>,
) {
    private val logger = KotlinLogging.logger {}
    private val targetScales = mutableMapOf<String, Int>()
    private val executorServices = Executors.newSingleThreadScheduledExecutor()

    init {
        executorServices.scheduleAtFixedRate({
            for (template in containerController.getTemplates()) {
                scaleServer(template.templateId)
            }
        }, 0, 1, TimeUnit.SECONDS)
    }

    fun scaleDeployment(
        templateId: String,
        scale: Int,
    ): SResult<Unit> {
        if (scale < 0) return Err("Scale cannot be less than 0")

        targetScales[templateId] = scale

        return Ok(Unit)
    }

    private fun scaleServer(templateId: String) {
        val targetScale = targetScales.getOrPut(templateId) { 0 }
        val pendingCreation = containerController.getPendingContainers(templateId, serverManager.getServers().map { it.containerId })
        val activeServers = serverManager.getActiveServersByTemplate(templateId)

        val currentScale = pendingCreation.count() + activeServers.count()

        val neededServers = currentScale - targetScale

        if (neededServers == 0) {
            logger.debug { "Target scale already met" }
            return
        }

        if (neededServers > 0) {
            // Scaling deployment up

            for (i in 0 until neededServers) {
                containerController.create(templateId).onFailure {
                    logger.warn { "Failed to create container" }
                    return
                }
            }
        } else {
            // Scaling deployment down
            val serversToRemove = -neededServers

            if (activeServers.size < serversToRemove) {
                logger.info { "Cannot scale down yet, servers are still being created" }
                return
            }

            for (registeredServer in activeServers.take(serversToRemove)) {
                serverManager.scheduleShutdown(registeredServer)
            }
        }
    }
}
