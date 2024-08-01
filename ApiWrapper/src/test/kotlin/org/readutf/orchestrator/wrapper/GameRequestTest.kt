package org.readutf.orchestrator.wrapper

import org.junit.jupiter.api.Test

/**
 * The server needs to be running before
 * game request tests are executed
 */
class GameRequestTest {
    @Test
    fun testGameRequest() {
        val (requestId, serverId, gameId) = OrchestratorApi.requestGame("test").join()

        println("server: $serverId | game: $gameId")
    }
}
