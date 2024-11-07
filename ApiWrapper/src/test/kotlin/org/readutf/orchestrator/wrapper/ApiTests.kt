package org.readutf.orchestrator.wrapper

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.readutf.orchestrator.wrapper.socket.NotificationSocket
import java.util.concurrent.CompletableFuture

/**
 * The server needs to be running before
 * game request tests are executed
 */
class ApiTests {
    val orchestratorApi = OrchestratorApi("localhost", 9393)

    @Test
    fun testGameRequest() {
        val (_, server, gameId) = orchestratorApi.requestGame("test").join()

        println("server: $server | game: $gameId")
    }

    @Test
    fun testNotificationSocket() {
        val future = CompletableFuture<Unit>()
        NotificationSocket(uri = "ws://localhost:9393/notifications", objectMapper = ObjectMapper().registerKotlinModule()) {
            future.complete(null)
        }
        future.join()
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
            val allServers = orchestratorApi.getServers().getOrThrow()

            val serverId = allServers[0].serverId

            val getServer = orchestratorApi.getServerById(serverId)
            Assertions.assertEquals(true, getServer.isSuccess)

            println(getServer)
        }
    }
}
