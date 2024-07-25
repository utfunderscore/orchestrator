package org.readutf.orchestrator.server.network

import com.esotericsoftware.kryo.kryo5.Kryo
import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.orchestrator.server.server.ServerManager

class ServerNetworkManager(
    val kryo: Kryo,
    val serverManager: ServerManager,
) {
    private val logger = KotlinLogging.logger { }
}
