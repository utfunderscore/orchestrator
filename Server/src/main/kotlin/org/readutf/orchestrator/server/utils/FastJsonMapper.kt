package org.readutf.orchestrator.server.utils

import io.javalin.json.JsonMapper
import org.readutf.orchestrator.server.Orchestrator.Companion.objectMapper
import java.lang.reflect.Type

object FastJsonMapper : JsonMapper {
    override fun <T : Any> fromJsonString(
        json: String,
        targetType: Type,
    ): T = objectMapper.readValue(json, objectMapper.typeFactory.constructType(targetType))

    override fun toJsonString(
        obj: Any,
        type: Type,
    ): String = objectMapper.writeValueAsString(obj)
}
