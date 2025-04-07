package org.readutf.orchestrator.server.loadbalancer

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.server.service.scale.ScaleManager
import org.readutf.orchestrator.server.service.template.TemplateManager

class AutoscaleTask(
    val autoscaleManager: AutoscaleManager,
    val scaleManager: ScaleManager,
    val serverManager: ServerManager,
    val templateManager: TemplateManager,
) : Runnable {
    private val logger = KotlinLogging.logger { }

    override fun run() {
        autoscaleManager.scalers.toList().forEach { (name, scaler) ->
            if (!templateManager.exists(name)) {
                autoscaleManager.scalers.remove(name)
                return
            }

            val existing = serverManager.getServersByTemplate(name)
            val targetResources = scaler.getNeededResources(existing)
            scaleManager.scaleService(name, targetResources)
        }
    }
}
