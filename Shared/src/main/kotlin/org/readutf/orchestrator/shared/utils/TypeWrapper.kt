package org.readutf.orchestrator.shared.utils

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize

class TypeWrapper(
    data: Any,
) {
    @JsonSerialize(using = TypeWrapperSerializer::class)
    @JsonDeserialize(using = TypeWrapperDeserializer::class)
    val data: Any = data

    override fun toString(): String = "TypeWrapper(data=$data)"
}
