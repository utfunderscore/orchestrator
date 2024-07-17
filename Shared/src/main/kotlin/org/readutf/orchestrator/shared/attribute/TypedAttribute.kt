package org.readutf.orchestrator.shared.attribute

class TypedAttribute<T>(
    val type: Class<out T>,
    val value: T,
)
