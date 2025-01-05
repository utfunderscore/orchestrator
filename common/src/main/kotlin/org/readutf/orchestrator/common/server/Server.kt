package org.readutf.orchestrator.common.server

import java.util.UUID

open class Server(
    val serverId: UUID,
    val displayName: String,
    val containerId: String,
)
