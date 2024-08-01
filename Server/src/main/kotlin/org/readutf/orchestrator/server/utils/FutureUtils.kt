package org.readutf.orchestrator.server.utils

import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

object FutureUtils {
    fun <T> executeUntilSuccess(
        futures: List<Supplier<CompletableFuture<T>>>,
        onFailure: T,
        predicate: (T) -> Boolean,
        index: Int = 0,
    ): CompletableFuture<T> {
        if (index > futures.lastIndex) {
            System.out.println("No futures left")
            return CompletableFuture.completedFuture(onFailure)
        }

        val currentFuture = futures[index].get()

        val future = CompletableFuture<T>()

        currentFuture.thenAccept { result ->
            if (predicate.invoke(result)) {
                future.complete(result)
            } else {
                executeUntilSuccess(futures, onFailure, predicate, index + 1).thenAccept(future::complete)
            }
        }

        return future
    }
}
