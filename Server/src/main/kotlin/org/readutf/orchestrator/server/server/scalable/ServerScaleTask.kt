package org.readutf.orchestrator.server.server.scalable

import org.readutf.orchestrator.server.server.template.ServerTemplateManager

class ServerScaleTask(
    private val serverTemplateManager: ServerTemplateManager,
    private val serverScaleManager: ServerScaleManager,
) : Runnable {
    override fun run() {
        serverTemplateManager.getTemplates().forEach {
            val serverType = it.templateId
            val targetScale = serverScaleManager.getTargetScale(serverType)
            serverScaleManager.refreshScaleState(serverType, targetScale)
        }
    }
}
