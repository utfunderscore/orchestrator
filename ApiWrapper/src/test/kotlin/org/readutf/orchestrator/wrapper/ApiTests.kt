package org.readutf.orchestrator.wrapper

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * The server needs to be running before
 * game request tests are executed
 */
class ApiTests {
    val orchestratorApi = OrchestratorApi("localhost", 9393)

    @Test
    fun testGameRequest() {
        val (requestId, server, gameId) = orchestratorApi.requestGame("test").join()

        println("server: $server | game: $gameId")
    }

    @Test
    fun testGetServers() {
        runBlocking {
            val response = orchestratorApi.serverService
            println(response)

            Assertions.assertEquals(true, response.getAllServers().success)
        }
    }

    @Test
    fun testGetServer() {
        runBlocking {
            val allServers = orchestratorApi.serverService.getAllServers()

            Assertions.assertEquals(true, allServers.success)
            val serverId = allServers.response!![0].serverId

            val getServer = orchestratorApi.serverService.getServer(serverId)
            Assertions.assertEquals(true, getServer.success)

            println(getServer)
        }
    }
}
