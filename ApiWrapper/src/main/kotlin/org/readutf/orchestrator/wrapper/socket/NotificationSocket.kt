package org.readutf.orchestrator.wrapper.socket

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.readutf.orchestrator.shared.notification.Notification
import org.readutf.orchestrator.shared.notification.NotificationWrapper
import java.lang.Exception
import java.net.URI
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class NotificationSocket internal constructor(
    uri: String,
    private val objectMapper: ObjectMapper,
    private val autoReconnect: Boolean = true,
    private val notificationListener: (Notification) -> Unit,
) : WebSocketClient(URI(uri)) {
    private val logger = KotlinLogging.logger { }
    private val reconnectExecutor = Executors.newSingleThreadScheduledExecutor()

    init {
        logger.info { "Connecting to notification websocket $uri" }
        connect()
    }

    override fun onOpen(p0: ServerHandshake?) {
        logger.info { "Connected to game request websocket" }
    }

    override fun onMessage(message: String) {
        try {
            objectMapper.readValue(message, NotificationWrapper::class.java)?.let { wrapped ->
                notificationListener(wrapped.notification)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            logger.error(e) { "Failed to parse notification" }
        }
    }

    override fun onClose(
        errorCode: Int,
        errorReason: String,
        remote: Boolean,
    ) {
        logger.info { "Disconnected from game request websocket ($errorCode, $errorReason, $remote" }
        if (autoReconnect) {
            logger.info { "Reconnecting in 5 seconds..." }
            reconnectExecutor.schedule(::connect, 5, TimeUnit.SECONDS)
        }
    }

    override fun onError(exception: Exception) {
        logger.error(exception) { }
    }
}
