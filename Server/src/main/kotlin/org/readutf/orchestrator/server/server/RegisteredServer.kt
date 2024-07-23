package org.readutf.orchestrator.server.server

import org.readutf.hermes.channel.HermesChannel
import org.readutf.orchestrator.shared.server.Server

class RegisteredServer(
    val channel: HermesChannel,
    val server: Server,
)
