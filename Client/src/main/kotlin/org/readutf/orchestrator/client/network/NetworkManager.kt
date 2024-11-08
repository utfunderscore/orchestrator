package org.readutf.orchestrator.client.network

import java.util.concurrent.CompletableFuture

class NetworkManager(
    orchestratorHost: String,
    orchestratorPort: Int,
) {
    private val shutdownFuture = CompletableFuture<Void>()
}
