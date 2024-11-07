package org.readutf.orchestrator.shared.notification.impl

import org.readutf.orchestrator.shared.notification.Notification

data class ServerUnregisterNotification(
    val serverId: String,
) : Notification
