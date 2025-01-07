package org.readutf.orchestrator.common.server

import org.readutf.orchestrator.common.utils.ShortId
import java.util.UUID

open class Server(
    val serverId: UUID,
    val displayName: String,
    val containerId: ShortId,
)
