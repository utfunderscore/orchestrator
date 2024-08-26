package org.readutf.orchestrator.shared.packets

import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.readutf.hermes.Packet
import java.util.*

data class ServerAttributeUpdate(
    val serverId: UUID,
    val attributeName: String,
    @JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.CLASS) val attribute: Any,
) : Packet()
