package org.readutf.orchestrator.shared.notification

import com.fasterxml.jackson.annotation.JsonTypeInfo

data class NotificationWrapper(
    @field:JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.MINIMAL_CLASS)
    val notification: Notification,
)
