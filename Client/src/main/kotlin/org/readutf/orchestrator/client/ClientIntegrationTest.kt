package org.readutf.orchestrator.client

import org.readutf.orchestrator.shared.server.ServerAddress

class ClientIntegrationTest {
    private var client: ShepardClient = ShepardClient(ServerAddress("localhost", 25565), listOf())

    init {
        Thread {
            Thread.sleep(5_000)
            println("Shutting down test...")
            client.shutdown()
        }.start()
    }
}

fun main() {
    ClientIntegrationTest()
}
