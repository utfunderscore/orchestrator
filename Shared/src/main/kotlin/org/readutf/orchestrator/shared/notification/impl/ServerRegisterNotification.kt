package org.readutf.orchestrator.shared.notification.impl

import org.readutf.orchestrator.shared.notification.Notification
import org.readutf.orchestrator.shared.server.Server

data class ServerRegisterNotification(
    val server: Server,
) : Notification
