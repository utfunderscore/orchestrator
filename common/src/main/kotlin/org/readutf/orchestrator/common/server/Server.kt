package org.readutf.orchestrator.common.server

import com.fasterxml.jackson.databind.JsonNode
import org.readutf.orchestrator.common.template.TemplateName
import java.util.UUID
import kotlin.math.min

@JvmInline
value class ContainerId(val id: String) {

    fun toShort(): ShortContainerId = ShortContainerId.of(id)
}

@JvmInline
value class ShortContainerId private constructor(val id: String) {

    companion object {

        fun of(id: String): ShortContainerId = ShortContainerId(id.substring(0, min(id.length, 12)))
    }
}

open class Server(
    val id: UUID,
    val displayName: String,
    val shortContainerId: ShortContainerId,
    val networkSettings: NetworkSettings,
    val templateName: TemplateName,
    val attributes: MutableMap<String, JsonNode>,
)
