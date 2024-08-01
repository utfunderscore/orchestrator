package org.readutf.orchestrator.server.utils

import com.alibaba.fastjson2.JSON
import io.javalin.json.JsonMapper
import java.lang.reflect.Type

object FastJsonMapper : JsonMapper {
    override fun <T : Any> fromJsonString(
        json: String,
        targetType: Type,
    ): T = JSON.parseObject(json, targetType)

    override fun toJsonString(
        obj: Any,
        type: Type,
    ): String = JSON.toJSONString(obj)
}
