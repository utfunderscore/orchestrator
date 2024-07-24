package org.readutf.orchestrator.shared.server

import java.util.UUID

data class ServerHeartbeat(
    val serverId: UUID,
    val timestamp: Long = System.currentTimeMillis(),
)
