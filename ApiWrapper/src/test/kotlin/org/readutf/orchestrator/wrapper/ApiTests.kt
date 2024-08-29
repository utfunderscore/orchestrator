package org.readutf.orchestrator.wrapper

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * The server needs to be running before
 * game request tests are executed
 */
class ApiTests {
    val orchestratorApi = OrchestratorApi("89.33.85.41", 9393)

    @Test
    fun testGameRequest() {
        val (requestId, server, gameId) = orchestratorApi.requestGame("test").join()

        println("server: $server | game: $gameId")
    }

    @Test
    fun testGetServers() {
        runBlocking {
            orchestratorApi.getServers()
        }
    }

    @Test
    fun testGetPort() {
        runBlocking {
            println(orchestratorApi.getPort("20a3eae4b5a4"))
        }
    }

    @Test
    fun testGetIp() {
        runBlocking {
            println(orchestratorApi.getIp("0f9d687a09ce"))
        }
    }

    @Test
    fun testGetServer() {
        runBlocking {
            val allServers = orchestratorApi.getServers().get()

            val serverId = allServers[0].serverId

            val getServer = orchestratorApi.getServerById(serverId)
            Assertions.assertEquals(true, getServer.isOk())

            println(getServer)
        }
    }
}
