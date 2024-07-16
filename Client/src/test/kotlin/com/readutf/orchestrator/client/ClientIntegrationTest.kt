package com.readutf.orchestrator.client

import io.github.oshai.kotlinlogging.KotlinLogging
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.readutf.orchestrator.client.ShepardClient
import org.readutf.orchestrator.shared.server.ServerAddress
import kotlin.test.BeforeTest

class ClientIntegrationTest {
    private lateinit var client: ShepardClient

    private val logger = KotlinLogging.logger { }

    @BeforeTest
    fun setup() {
        client = ShepardClient(ServerAddress("localhost", 25565))

        logger.info { "Server is starting...." }
    }

    @Test
    fun testSleep() {
        Thread.sleep(5000)
    }

    @AfterEach
    fun tearDown() {
        logger.info { "Server is shutting down...." }
        client.shutdown()
    }
}
