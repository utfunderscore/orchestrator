package org.readutf.orchestrator.shared.utils

import com.fasterxml.jackson.annotation.JsonTypeInfo

class TypeWrapper(
    data: Any,
) {
    @field:JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.CLASS)
    val attribute: Any = data

    override fun toString(): String = "TypeWrapper(data=$attribute)"
}
