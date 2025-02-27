package org.readutf.orchestrator.client.client.game

import java.util.UUID
import java.util.concurrent.CompletableFuture

public fun interface GameRequestHandler {

    public fun startGame(): CompletableFuture<UUID>
}
