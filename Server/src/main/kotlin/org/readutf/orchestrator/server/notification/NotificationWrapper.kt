package org.readutf.orchestrator.server.notification

import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.readutf.orchestrator.shared.notification.Notification

class NotificationWrapper(
    @field:JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.CLASS)
    val notification: Notification,
)
