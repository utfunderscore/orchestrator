package org.readutf.orchestrator.server.server.scalable

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.orchestrator.server.docker.DockerManager
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.server.server.template.ServerTemplateManager
import org.readutf.orchestrator.shared.server.Server
import org.readutf.orchestrator.shared.utils.Result
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ServerScaleManager(
    private val templateManager: ServerTemplateManager,
    private val dockerManager: DockerManager,
    private val serverManager: ServerManager,
) {
    private val logger = KotlinLogging.logger {}
    private val schedular = Executors.newSingleThreadScheduledExecutor()

    init {
        schedular.scheduleAtFixedRate(ServerScaleTask(templateManager, this), 0, 5, TimeUnit.SECONDS)
    }

    // Server Type -> Container ID & Creation Start time
    private val pendingCreation = mutableMapOf<String, MutableMap<String, Long>>()

    // Server Type -> Scale
    private val targetScale = mutableMapOf<String, Int>()

    // Server Type -> Futures Awaiting Creation of server
    private val awaitingServers = mutableMapOf<String, List<CompletableFuture<Server>>>()

    fun setScale(
        serverType: String,
        scale: Int,
    ) {
        targetScale[serverType] = scale

        refreshScaleState(serverType, scale)
    }

    internal fun getTargetScale(serverType: String): Int = targetScale.getOrPut(serverType) { 0 }

    internal fun refreshScaleState(
        serverTypeId: String,
        targetScale: Int,
    ): Result<Unit, String> {
        val serverType =
            templateManager.getTemplate(serverTypeId) ?: return Result.failure("Server type not found")

        val currentScale =
            serverManager.getServersByType(serverTypeId).filter {
                !it.pendingDeletion
            }

        val awaitingCreation = getPendingCreation(serverTypeId)
        logger.info { "Target scale: $targetScale" }
        logger.info { "Current scale: ${currentScale.count()} " }
        logger.info { "Awaiting creation: ${awaitingCreation.size}" }

        val existingCount = currentScale.count()
        val existingAndPendingCount = currentScale.count() + awaitingCreation.count()

        if (existingCount == targetScale) {
            // No futures should exist if we are already at the correct scale
            awaitingServers[serverTypeId]?.forEach {
                it.completeExceptionally(IllegalStateException("Awaiting creation when none are pending"))
            }
        }
        if (existingAndPendingCount == targetScale) {
            logger.debug { "Server type $serverTypeId is already at target scale" }
            return Result.empty()
        }

        if (targetScale > existingAndPendingCount) {
            val toCreate = targetScale - existingAndPendingCount
            logger.info { "Creating $toCreate servers of type $serverTypeId" }

            (0 until toCreate)
                .asSequence()
                .map {
                    serverManager.createServer(serverType)
                }.filter {
                    it.containerId != null
                }.forEach {
                    pendingCreation.getOrPut(serverTypeId) { mutableMapOf() }[it.containerId!!.shortId] = System.currentTimeMillis()
                }
            return Result.empty()
        } else {
            val toDelete = existingAndPendingCount - targetScale
            logger.info { "Deleting $toDelete servers of type $serverTypeId" }
            currentScale.take(toDelete).forEach {
                serverManager.shutdownServer(it.serverId)
            }
            return Result.empty()
        }
    }

    private fun getPendingCreation(serverType: String): Map<String, Long> {
        val pending =
            pendingCreation
                .getOrPut(serverType) { mutableMapOf() }
                .filterKeys { serverManager.getServerById(it) == null }
                .filter { System.currentTimeMillis() - it.value < 30_000 }

        pendingCreation[serverType] = pending.toMutableMap()

        return pending
    }
}
