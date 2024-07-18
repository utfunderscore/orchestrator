package org.readutf.orchestrator.server.network.exception

import io.github.oshai.kotlinlogging.KotlinLogging
import java.net.SocketException
import java.util.function.Consumer

class SocketExceptionHandler : Consumer<SocketException> {
    private var logger = KotlinLogging.logger { }

    override fun accept(t: SocketException) {
        when (t.message) {
            "Connection reset" -> logger.debug(t) { "Connection reset by peer" }
            else -> logger.error(t) { "Socket exception" }
        }
    }
}
