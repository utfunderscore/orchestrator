package org.readutf.orchestrator.client.game

import java.util.*

interface GameRequestHandler {
    /**
     * Consumes game requests and produces a game future
     */
    fun handleRequest(gameType: String): UUID?
}
