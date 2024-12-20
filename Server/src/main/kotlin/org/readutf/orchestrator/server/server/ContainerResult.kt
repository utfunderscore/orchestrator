@file:Suppress("ktlint:standard:filename")

package org.readutf.orchestrator.server.server

import org.readutf.orchestrator.server.docker.ContainerId
import org.readutf.orchestrator.shared.utils.Result
import java.util.concurrent.CompletableFuture

data class ContainerResult<T>(
    val containerId: ContainerId?,
    val creationError: String?,
    val creationFuture: CompletableFuture<Result<T, String>>?,
) {
    companion object {
        fun <T> failedPreCreation(reason: String): ContainerResult<T> =
            ContainerResult(containerId = null, creationError = reason, creationFuture = null)

        fun <T> awaitingResult(
            containerId: ContainerId,
            future: CompletableFuture<Result<T, String>>,
        ): ContainerResult<T> = ContainerResult(containerId, null, future)
    }
}
