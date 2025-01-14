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
            for (templateId in containerController.getTemplates()) {
                scaleServer(templateId)
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

    fun getScale(templateId: String): Int = targetScales.getOrPut(templateId) { 0 }

    private fun scaleServer(templateId: String) {
        val targetScale = targetScales.getOrPut(templateId) { 0 }
        val pendingCreation = containerController.getPendingContainers(templateId, serverManager.getServers().map { it.containerId })
        val activeServers = serverManager.getActiveServersByTemplate(templateId)
        logger.debug { "Active Servers: $activeServers" }

        val currentScale = pendingCreation.count() + activeServers.count()

        val neededServers = targetScale - currentScale
        logger.debug { "Needed servers: $neededServers" }

        if (neededServers == 0) {
            logger.debug { "Target scale already met" }
            return
        }

        if (neededServers > 0) {
            // Scaling deployment up

            for (i in 0 until neededServers) {
                containerController.create(templateId).onFailure {
                    logger.warn { "Failed to create container $it" }
                    return
                }
            }
        } else {
            // Scaling deployment down
            val serversToRemove = -neededServers

            if (activeServers.size < serversToRemove) {
                logger.debug { "Cannot scale down yet, servers are still being created" }
                return
            }

            for (registeredServer in activeServers.take(serversToRemove)) {
                serverManager.scheduleShutdown(registeredServer)
            }
        }
    }
}
