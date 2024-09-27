package org.readutf.orchestrator.shared.notification.impl

import org.readutf.orchestrator.shared.notification.Notification
import java.util.UUID

data class ServerUnregisterNotification(
    val serverId: UUID,
) : Notification
