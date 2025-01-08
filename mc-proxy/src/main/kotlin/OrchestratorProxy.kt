import com.google.common.eventbus.Subscribe
import com.google.inject.Inject
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.ProxyServer
import java.io.File
import java.util.logging.Logger


@Plugin(id = "orchestrator", name = "Orchestrator Proxy")
class OrchestratorProxy @Inject constructor(private val proxyServer: ProxyServer, logger: Logger, val directory: File) {

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent?) {

    }

}