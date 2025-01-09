import com.google.inject.Inject
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.ProxyServer
import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.orchestrator.client.ConnectionManager
import org.readutf.orchestrator.client.OrchestratorClient
import org.readutf.orchestrator.client.capacity.DefaultCapacityHandler
import org.readutf.orchestrator.client.platform.DockerPlatform
import safeshutdown.SafeShutdown

@Plugin(id = "orchestrator", name = "Orchestrator Proxy", authors = ["utfunderscore"])
class OrchestratorProxy
    @Inject
    constructor(
        private val proxyServer: ProxyServer,
    ) {
        private val hostAddress = System.getenv("orchestrator.hostaddress") ?: "orchestrator"
        private val maxPlayers = System.getenv("orchestrator.maxplayers")?.toInt() ?: 200
        private val logger = KotlinLogging.logger { }
        private val safeShutdown = SafeShutdown(proxyServer)

        init {
            val orchestratorClient =
                OrchestratorClient(
                    hostAddress = hostAddress,
                    maxReconnectAttempts = Int.MAX_VALUE,
                    platform = DockerPlatform(),
                    capacityHandler =
                        DefaultCapacityHandler {
                            return@DefaultCapacityHandler proxyServer.playerCount.toDouble() / maxPlayers
                        },
                )

            orchestratorClient.shutdownHook = {
                logger.info { "Shutting down" }
                proxyServer.shutdown()
            }
            orchestratorClient.onConnect(this::onConnect)

            Thread {
                orchestratorClient.connectBlocking()
            }.start()
        }

        fun onConnect(connectionManager: ConnectionManager) {
            connectionManager.registerSafeShutdownListener(safeShutdown)
        }
    }
