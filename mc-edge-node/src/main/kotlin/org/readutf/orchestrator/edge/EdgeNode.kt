package org.readutf.orchestrator.edge

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.orchestrator.client.OrchestratorClient
import org.readutf.orchestrator.client.capacity.DefaultCapacityHandler
import org.readutf.orchestrator.client.platform.DockerPlatform
import org.readutf.orchestrator.edge.finder.impl.OrchestratorFinder
import org.readutf.orchestrator.edge.network.ConnectionManager
import org.readutf.orchestrator.edge.packets.ClientAcknowledgeListener
import org.readutf.orchestrator.edge.packets.ServerStatusListener
import org.readutf.orchestrator.proxy.OrchestratorApi
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.exitProcess

class EdgeNode {
    private val connectionManager = ConnectionManager("0.0.0.0", 25565)

    private val orchestratorApi = OrchestratorApi("orchestrator", 9191)

    private val transferFinder = OrchestratorFinder(orchestratorApi)

    private val hostAddress = System.getenv("orchestrator.hostaddress") ?: "orchestrator"

    private val logger = KotlinLogging.logger { }

    init {

        val properties = Properties()
        properties.load(EdgeNode::class.java.getResourceAsStream("/version.properties"))

        val version = properties.getOrDefault("version", "UNKNOWN")
        val builtAt = properties.getOrDefault("buildTime", "UNKNOWN") as String

        val formattedBuildTime = SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss").format(Date(builtAt.toLong()))
        println("   Running Orchestrator Edge Node v$version built on $formattedBuildTime")

        val orchestratorClient =
            OrchestratorClient(
                hostAddress = hostAddress,
                platform = DockerPlatform(),
                capacityHandler =
                    DefaultCapacityHandler {
                        return@DefaultCapacityHandler 0.5
                    },
            )

        orchestratorClient.shutdownHook = {
            logger.info { "Shutdown signal received, shutting down..." }
            connectionManager.stop()
            exitProcess(0)
        }

        Thread {
            orchestratorClient.connectBlocking()
        }.start()

        orchestratorClient.onConnect {
            connectionManager.registerListener(ClientAcknowledgeListener(transferFinder))
            connectionManager.registerListener(ServerStatusListener())
            Thread {
                connectionManager.start()
            }.start()
        }
    }
}

fun main() {
    EdgeNode()
}
