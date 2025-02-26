package org.readutf.orchestrator.common.server

import com.fasterxml.jackson.databind.JsonNode
import org.readutf.orchestrator.common.utils.ShortId
import java.util.UUID

open class Server(
    val serverId: UUID,
    val displayName: String,
    val containerId: ShortId,
    val networkSettings: NetworkSettings,
    val attributes: MutableMap<String, JsonNode>,
)
