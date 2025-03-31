package org.readutf.orchestrator.server.serverfinder.impl

import com.fasterxml.jackson.databind.JsonNode
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.orchestrator.common.server.Server
import org.readutf.orchestrator.common.template.TemplateName
import org.readutf.orchestrator.server.loadbalancer.LoadBalancer
import org.readutf.orchestrator.server.loadbalancer.LoadBalancerManager
import org.readutf.orchestrator.server.server.RegisteredServer
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.server.serverfinder.ServerFinder
import org.readutf.orchestrator.server.service.ContainerManager
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.atomic.AtomicInteger

class DefaultServerFinder(
    val templateName: TemplateName,
    val loadBalancerManager: LoadBalancerManager,
    val serverManager: ServerManager,
    val templateManager: ContainerManager,
    val minFillThreshold: Int = 5,
) : ServerFinder {
    private val logger = KotlinLogging.logger { }

    private val taskIdTracker = AtomicInteger(0)
    private val executor = Executors.newCachedThreadPool()
    private val newServerExecutor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private val taskIds = mutableMapOf<Int, ScheduledFuture<*>>()

    override fun findServer(args: JsonNode): CompletableFuture<Result<Server, Throwable>> {
        val loadBalancer = loadBalancerManager.getLoadBalancer(templateName) ?: let {
            return CompletableFuture.completedFuture(Err(IllegalStateException("No load balancer")))
        }

        return CompletableFuture.supplyAsync({
            val serverResult = findBestServer()
            if (serverResult != null) {
                return@supplyAsync Ok(serverResult)
            }

            return@supplyAsync awaitBestServer(loadBalancer).join()
        }, executor)
    }

    fun findBestServer(): RegisteredServer? = templateManager.getContainersByType(templateName)
        .mapNotNull { container ->
            serverManager.getServers()
                .firstOrNull { it.shortContainerId == container.containerId }
        }.minByOrNull { it.getCapacity() }

    private fun awaitBestServer(loadBalancer: LoadBalancer): CompletableFuture<Result<Server, Throwable>> {
        loadBalancer.addAwaitingRequest()

        val future = CompletableFuture<Result<Server, Throwable>>()
        val taskId = taskIdTracker.getAndIncrement()
        val task = BestServerTask(taskId, future)
        taskIds[taskId] = newServerExecutor.scheduleAtFixedRate(task, 1, 20, java.util.concurrent.TimeUnit.MILLISECONDS)
        return future
    }

    inner class BestServerTask(
        private val taskId: Int,
        private val future: CompletableFuture<Result<Server, Throwable>>,
    ) : Runnable {
        private var start = System.currentTimeMillis()

        override fun run() {
            val serverResult = findBestServer()

            if (serverResult != null) {
                taskIds.remove(taskId)?.cancel(true)
                future.complete(Ok(serverResult))
                logger.info { "Server found after ${System.currentTimeMillis() - start}ms" }
            } else if (System.currentTimeMillis() - start > 10_000) {
                taskIds.remove(taskId)?.cancel(true)
                logger.warn { "Could not find server after 10 seconds of waiting" }
                future.complete(Err(Throwable("Could not find an available server")))
            }
        }
    }
}
