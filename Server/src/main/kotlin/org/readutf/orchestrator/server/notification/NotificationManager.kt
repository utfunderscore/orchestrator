package org.readutf.orchestrator.server.notification

import io.javalin.websocket.WsConfig
import io.javalin.websocket.WsContext
import org.readutf.orchestrator.shared.notification.Notification
import java.util.function.Consumer

object NotificationManager {
    val activeListeners = mutableListOf<WsContext>()

    fun notifyAll(notification: Notification) {
        activeListeners.forEach {
            it.send(NotificationWrapper(notification = notification))
        }
    }

    class NotificationSocket : Consumer<WsConfig> {
        override fun accept(wsConfig: WsConfig) {
            wsConfig.onConnect {
                activeListeners.add(it)
            }
            wsConfig.onClose {
                activeListeners.remove(it)
            }
        }
    }
}
