package org.readutf.orchestrator.integration

import org.readutf.orchestrator.client.ShepardClient
import org.readutf.orchestrator.shared.game.Game
import org.readutf.orchestrator.shared.game.GameState
import org.readutf.orchestrator.shared.server.ServerAddress
import java.util.*

class ClientIntegrationTest {
    private var client: ShepardClient =
        ShepardClient(
            ServerAddress("localhost", 25565),
            listOf(),
        ) { List(5) { Game(UUID.randomUUID(), "", emptyList(), GameState.ENDED) } }

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
