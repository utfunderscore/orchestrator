package org.readutf.orchestrator.server.server.scalable

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.orchestrator.server.docker.DockerManager
import org.readutf.orchestrator.server.server.template.ServerTemplateManager
import java.util.concurrent.Executors

class ServerScaleManager(
    private val templateManager: ServerTemplateManager,
    private val dockerManager: DockerManager,
) {
    private val logger = KotlinLogging.logger {}
    private val schedular = Executors.newSingleThreadScheduledExecutor()

    init {
        schedular.scheduleAtFixedRate(
            { cleanupInactiveContainers() },
            0,
            3,
            java.util.concurrent.TimeUnit.MINUTES,
        )
    }

    fun cleanupInactiveContainers() {
        val templates = templateManager.getTemplates().filter { it.dockerImage == "lobby:9" }

        for (template in templates) {
            val containers =
                dockerManager.getContainersByImageId(template.dockerImage).onFailure {
                    logger.error { "Failed to get containers by image id: $it" }
                    return@onFailure
                }

            logger.info { "Found ${containers.size} containers for template ${template.templateId}" }

            val inactiveContainers = containers.filter { it.state.equals("exited", true) }
            for (inactiveContainer in inactiveContainers) {
                logger.info { "Deleting inactive container ${inactiveContainer.id}" }
                val deleteResult = dockerManager.deleteContainer(inactiveContainer.id)
                if (deleteResult.isFailure) {
                    logger.error { "Failed to delete container ${inactiveContainer.id}: ${deleteResult.getError()}" }
                }
            }
        }

        templates.forEach { template ->
            val containers =
                dockerManager.getContainersByImageId(template.dockerImage).onFailure {
                    logger.error { "Failed to get containers by image id: $it" }
                    return
                }

            containers.forEach { container ->
                println("Container: ${container.id} - ${container.imageId}")
            }
        }
    }
}
