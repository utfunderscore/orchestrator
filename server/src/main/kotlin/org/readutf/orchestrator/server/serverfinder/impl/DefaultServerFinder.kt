package org.readutf.orchestrator.server.serverfinder.impl

import com.fasterxml.jackson.databind.JsonNode
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.orchestrator.common.server.Server
import org.readutf.orchestrator.server.loadbalancer.LoadBalancer
import org.readutf.orchestrator.server.loadbalancer.LoadBalancerManager
import org.readutf.orchestrator.server.server.ServerManager
import org.readutf.orchestrator.server.serverfinder.ServerFinder
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.atomic.AtomicInteger

class DefaultServerFinder(
    val serverType: String,
    val loadBalancerManager: LoadBalancerManager,
    val serverManager: ServerManager,
    val minFillThreshold: Int = 5,
) : ServerFinder {
    private val logger = KotlinLogging.logger { }

    private val taskIdTracker = AtomicInteger(0)
    private val executor = Executors.newCachedThreadPool()
    private val newServerExecutor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private val taskIds = mutableMapOf<Int, ScheduledFuture<*>>()

    override fun findServer(args: JsonNode): CompletableFuture<Result<Server, Throwable>> {
        val loadBalancer = loadBalancerManager.getLoadBalancer(serverType)

        return CompletableFuture.supplyAsync({
            val serverResult = findBestServer()

            serverResult.onSuccess { server ->
                logger.info { "Pre-existing server found" }
                return@supplyAsync Ok(server)
            }

            return@supplyAsync awaitBestServer(loadBalancer).join()
        }, executor)
    }

    private fun findBestServer(): Result<Server, Throwable> = serverManager
        .getActiveServersByTemplate(serverType) // Get all servers of the type
        .minByOrNull { it.getCapacity() }
        ?.let { Ok(it) } ?: Err(Exception("Could not find server"))

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

            serverResult.onSuccess { server ->
                taskIds.remove(taskId)?.cancel(false)
                future.complete(Ok(server))
                logger.info { "Server found after ${System.currentTimeMillis() - start}ms" }
            }.onFailure { ex ->
                if (System.currentTimeMillis() - start > 10_000) {
                    taskIds.remove(taskId)?.cancel(false)
                    future.complete(Err(ex))
                }
            }
        }
    }
}
