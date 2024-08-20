package org.readutf.orchestrator.server.settings

import java.net.URI
import java.time.Duration

object Docker {
    val URI = URI("tcp://localhost:2375")
    val maxConnections = 100
    val responseTimeout: Duration = Duration.ofSeconds(15)
    val connectionTimeout: Duration = Duration.ofSeconds(15)
}
